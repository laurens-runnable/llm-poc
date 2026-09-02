#!/usr/bin/env bash

cd "${BASH_SOURCE%/*}/.." || exit
docker-compose logs -f
