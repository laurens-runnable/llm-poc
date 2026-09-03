#!/usr/bin/env bash

cd "${BASH_SOURCE%/*}/.." || exit

docker run --network=host --rm ubercadence/cli:master --do archeo-domain workflow list
