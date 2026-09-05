#!/usr/bin/env bash

cd "${BASH_SOURCE%/*}/.." || exit

docker run --network=host --rm ubercadence/cli:master \
  --do archeo-domain workflow start \
  --tasklist ArcheoTaskList \
  --workflow_type DocumentWorkflow::run \
  --execution_timeout 3600 \
  --input \"$1\"
