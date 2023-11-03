#!/usr/bin/env bash

NAME="$1"

DB=$(awk -F '/' '/db.url/ {print $NF}' ./config/database.properties)
USER=$(awk -F "=" '/db.user/ {print $2}' ./config/database.properties | tr -d ' ')
PASSWORD=$(awk -F "=" '/db.password/ {print $2}' ./config/database.properties | tr -d ' ')

docker run \
  --name "${NAME}" \
  --rm \
  --publish 127.0.0.1:5432:5432 \
  --volume ./database:/var/lib/postgresql/data \
  --env "POSTGRES_DB=${DB}" \
  --env "POSTGRES_USER=${USER}" \
  --env "POSTGRES_PASSWORD=${PASSWORD}" \
  postgres

