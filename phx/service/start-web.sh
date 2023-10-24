#!/usr/bin/env bash

NAME="$1"

docker run \
  --name "${NAME}" \
  --detach \
  --rm \
  --publish 8443:443 \
  --volume ./certificate:/usr/local/tomcat/certificate:ro \
  --volume ./config:/usr/local/tomcat/lib/physalix:ro \
  --volume ./logs:/usr/local/tomcat/logs \
  --volume ./logs:/usr/local/tomcat/webapps/logs \
  phx:latest

