#!/usr/bin/env bash

set -ex

echo "cleaning build folder ..."
(rm -rf ./build/*)
echo "building container ..."
./build.sh

echo "stopping web ..."
systemctl stop phx-web
echo "restart database ..."
systemctl restart phx-database
echo "starting web ..."
systemctl start phx-web
echo "done"