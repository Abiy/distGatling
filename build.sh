#!/bin/bash

cd gatling-rest/static
#ng build -c production --build-optimizer --optimization
ng build -c production --aot=false --build-optimizer=false
# workaround for https://github.com/willsoto/ng-chartist/issues/198
cd ../..

rm -rf gatling-rest/src/main/resources/static/*
cp -r gatling-rest/static/dist/gatlingUi/*  gatling-rest/src/main/resources/static/

mvn clean package

