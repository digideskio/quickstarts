#!/bin/bash
mvn clean install docker:build
docker push $DOCKER_REGISTRY/quickstart/java-netty-http-restdsl:2.0-SNAPSHOT
mvn fabric8:run
