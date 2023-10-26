#!/usr/bin/env bash

NAME="$1"

docker run \
  --name "${NAME}" \
  --rm \
  --add-host=host.docker.internal:host-gateway \
  --publish 443:8443 \
  --volume ./certificate:/usr/local/tomcat/certificate:ro \
  --volume ./config:/usr/local/tomcat/lib/physalix \
  --volume ./logs:/usr/local/tomcat/logs \
  --volume ./logs:/usr/local/tomcat/webapps/logs \
  phx:latest

