FROM pytorch/pytorch:2.14.0-cuda12.6-cudnn9-runtime
#FROM python:3.14.7

WORKDIR /code

COPY ./requirements.txt requirements.txt

RUN pip install --no-cache-dir --upgrade -r requirements.txt --ignore-requires-python

COPY ./app ./app

CMD ["fastapi", "run", "--entrypoint", "app.main:api"]
