from transformers import AutoModelForTokenClassification, AutoTokenizer

def load_hf_model(model_name: str, local_path: str):
    print(f"Loading model: {model_name}")
    model = AutoModelForTokenClassification.from_pretrained(model_name)
    print(f"Saving model: {model_name}")
    model.save_pretrained(local_path)

    tokenizer = AutoTokenizer.from_pretrained(model_name)
    print(f"Saving tokenizer: {local_path}")
    tokenizer.save_pretrained(local_path)

load_hf_model('alexbrandsen/ArcheoBERTje-NER', './data/ArcheoBERTje-NER')
