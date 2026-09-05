import asyncio
import logging
import os

from typing import Any
from light_s3_client import Client as S3Client
from cadence.client import Client as CadenceClient
from cadence.worker import Worker
from cadence import Registry, workflow
from transformers import pipeline, AutoModelForTokenClassification, AutoTokenizer
from unstructured.partition.pdf import partition_pdf

S3_SERVER = "http://localhost:8333"
S3_ACCESS_KEY = "admin"
S3_SECRET_KEY = "admin"

CADENCE_TARGET = "localhost:7833"
CADENCE_DOMAIN = "archeo-domain"
CADENCE_TASK_LIST = "ner-task-list"
CADENCE_REPORT_WORKFLOW_SIGNAL = "ReportWorkflow::setNamedEntities"

logging.basicConfig()
logging.root.setLevel(logging.WARNING)
# Get rid of excessive PDF logging
logging.getLogger("pdfminer.pdfinterp").setLevel(logging.ERROR)
logger = logging.getLogger("ner")
logger.setLevel(logging.INFO)

registry = Registry()

model_path = "./data/ArcheoBERTje-NER"
model = AutoModelForTokenClassification.from_pretrained(model_path, local_files_only=True)
tokenizer = AutoTokenizer.from_pretrained(model_path, local_files_only=True)


def extract_named_entities(s3_filename: str) -> list[Any]:
    logger.info("Downloading file %s", s3_filename)
    tmp_filename = './tmp/' + s3_filename
    try:
        s3_client = S3Client(
            server=S3_SERVER,
            access_key=S3_ACCESS_KEY,
            secret_key=S3_SECRET_KEY,
            region="europe-west1",
        )
        s3_client.download_file("work", s3_filename, tmp_filename)
        text = ''
        elements = partition_pdf(filename=tmp_filename, languages=["nld"])
        for element in elements:
            text += element.text + "\n"

        ner_pipeline = pipeline(
            "token-classification",
            model=model,
            tokenizer=tokenizer,
            aggregation_strategy="simple"
        )

        named_entities = []
        for entity in ner_pipeline(text):
            named_entities.append({
                entity['entity_group']: entity['word'],
            })
        return named_entities
    finally:
        os.remove(tmp_filename)


@registry.workflow()
class NerWorkflow:
    @workflow.run
    async def run(self, args) -> None:
        filename, workflow_id = args
        named_entities = extract_named_entities(filename)
        logger.info("Sending %s signal to workflow %s: %s", CADENCE_REPORT_WORKFLOW_SIGNAL, workflow_id, named_entities)
        await workflow.signal_external_workflow(workflow_id, CADENCE_REPORT_WORKFLOW_SIGNAL, named_entities)

async def main() -> None:
    async with CadenceClient(target=CADENCE_TARGET, domain=CADENCE_DOMAIN) as client:
        logger.info("Worker running on task list %s", CADENCE_TASK_LIST)
        async with Worker(client, CADENCE_TASK_LIST, registry, max_concurrent_activity_execution_size=1):
            await asyncio.Event().wait()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
