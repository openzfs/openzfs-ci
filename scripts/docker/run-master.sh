#!/bin/bash -eux

docker run -p 8080:8080 -p 5000:5000 $@ "openzfs/jenkins-master:latest"
