# Named Entity Recognition service

## Prerequisites

* Python 3.14

## Setup

```bash
# Dependencies
# Ignore python version, due to cadence-workflow-client Requires-Python >=3.11,<3.14
pip install -r requirements.txt --ignore-requires-python 

# Load model from Hugging Face and save to 'data' directory for offline use.
python3 scripts/load_model.py
```

## Run

```bash
# Run
fastapi run --entrypoint app.main:api

# Hot reload
fastapi run --entrypoint app.main:api --reload
```

## Docker image

> Building the Docker image only works on `x64` platforms.

```bash
# Build image
docker build -t nl.runnable.archeo:ner .
```
