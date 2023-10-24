#!/usr/bin/env bash

ZIP_URL=$(curl -s "https://api.github.com/repos/physalix-enrollment/physalix/releases/latest" | grep "zipball_url" | cut -d : -f 2,3 | tr -d \" | tr -d ,)
curl -L -o build/source.zip "${ZIP_URL}"
(cd build && unzip source.zip)

mkdir -p build/AdminGui/target
mkdir -p build/UserGui/target
curl -L -o build/AdminGui/target/AdminGui.war https://github.com/physalix-enrollment/physalix/releases/latest/download/AdminGui.war
curl -L -o build/UserGui/target/UserGui.war https://github.com/physalix-enrollment/physalix/releases/latest/download/UserGui.war

(cd build && docker build -t phx:latest .)
