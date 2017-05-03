#!/usr/bin/env bash

/bin/bash agent.sh -Dakka.contact-points=10.165.150.120:2551 -Dserver.port=8090 -Dactor.port=2556 -Dactor.role=soar
/bin/bash agent.sh -Dakka.contact-points=10.165.150.120:2551 -Dserver.port=8090 -Dactor.port=2556 -Dactor.role=aire

 #/bin/bash master.sh -Dmaster.port=2551 -Dserver.port=8080