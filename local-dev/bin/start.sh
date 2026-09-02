#!/usr/bin/env bash

cd "${BASH_SOURCE%/*}/.." || exit

if [ $1 ]
then
      docker-compose -f docker-compose.yml -f docker-compose.$1.yml up -d
else
      docker-compose up -d
fi
