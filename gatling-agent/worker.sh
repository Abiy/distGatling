#!/usr/bin/env bash

/bin/bash agent.sh -Dakka.contact-points=10.165.150.120:2551 -Dserver.port=8092 -Dactor.port=2558 -Dactor.role=autopi -Dactor.numberOfActors=2
/bin/bash agent.sh -Dakka.contact-points=10.165.150.120:2551 -Dserver.port=8090 -Dactor.port=2556 -Dactor.role=soar
/bin/bash agent.sh -Dakka.contact-points=172.16.0.85:2551 -Dserver.port=8091 -Dactor.port=2557 -Dactor.role=aire

 #/bin/bash master.sh -Dmaster.port=2551 -Dserver.port=8080