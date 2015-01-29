#!/bin/bash
mvn clean install docker:build
docker push $DOCKER_REGISTRY/quickstart/java-camel-netty-http-rest:2.0-SNAPSHOT
mvn fabric8:deploy
