#!/usr/bin/env bash

SERVICE_DIR="/phx/service"

# Define the names of your service files
WEBSERVICE_SERVICE="phx-web.service"
DATABASE_SERVICE="phx-database.service"

# Create symbolic links in /etc/systemd/system/
ln -s $SERVICE_DIR/$WEBSERVICE_SERVICE /etc/systemd/system/$WEBSERVICE_SERVICE
ln -s $SERVICE_DIR/$DATABASE_SERVICE /etc/systemd/system/$DATABASE_SERVICE

systemctl daemon-reload
systemctl enable $WEBSERVICE_SERVICE
