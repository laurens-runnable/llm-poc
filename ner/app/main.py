import logging

from io import BytesIO
from typing import Annotated
from fastapi import FastAPI, Request, Depends, Header, HTTPException, BackgroundTasks
from transformers import pipeline, AutoModelForTokenClassification, AutoTokenizer
from unstructured.partition.pdf import partition_pdf
from cadence import Client

CADENCE_DOMAIN = "archeo-domain"
CADENCE_TARGET = "localhost:7833"
CADENCE_REPORT_WORKFLOW_SIGNAL = "ReportWorkflow::setNamedEntities"

logging.basicConfig()
logging.root.setLevel(logging.WARNING)

# Get rid of excessive PDF logging
logging.getLogger("pdfminer.pdfinterp").setLevel(logging.ERROR)

logger = logging.getLogger("ner")
logger.setLevel(logging.INFO)

model_path = "./data/ArcheoBERTje-NER"
model = AutoModelForTokenClassification.from_pretrained(model_path, local_files_only=True)
tokenizer = AutoTokenizer.from_pretrained(model_path, local_files_only=True)

api = FastAPI()


async def verify_content_type(content_type: Annotated[str, Header()]):
    mime_types = ['application/pdf', 'text/plain']
    if mime_types.count(content_type) == 0:
        raise HTTPException(status_code=415, detail="Unsupported content")


@api.post("/ner/{workflow_id}", status_code=202, dependencies=[Depends(verify_content_type)])
async def ner(workflow_id: str, request: Request, content_type: Annotated[str, Header()],
              background_tasks: BackgroundTasks):
    body = await request.body()
    await process_content(workflow_id, content_type, body)
    # background_tasks.add_task(process_content, workflow_id, content_type, body)


async def process_content(workflow_id: str, content_type: str, body: bytes):
    if content_type == 'application/pdf':
        elements = partition_pdf(file=BytesIO(body), languages=["nld"])
        text = ''
        for element in elements:
            text += element.text + "\n"
    else:
        text = body.decode("utf-8")

    ner_pipeline = pipeline(
        "token-classification",
        model=model,
        tokenizer=tokenizer,
        aggregation_strategy="simple"
    )

    named_entities = []

    entities = ner_pipeline(text)
    for entity in entities:
        named_entities.append({
            entity['entity_group']: entity['word'],
        })

    logger.info("Sending %s signal to workflow %s: %s", CADENCE_REPORT_WORKFLOW_SIGNAL, workflow_id, named_entities)
    async with Client(domain=CADENCE_DOMAIN, target=CADENCE_TARGET) as client:
        await client.signal_workflow(
            workflow_id,
            "",
            CADENCE_REPORT_WORKFLOW_SIGNAL,
            named_entities
        )
