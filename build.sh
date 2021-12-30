#!/usr/bin/env bash

./gradlew build --warning-mode=all

cd nothanks-python || exit
python3 -m venv venv
./venv/bin/pip3 install -r requirements.txt
