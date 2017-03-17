#!/bin/bash -e

TOP=$(git rev-parse --show-toplevel 2>/dev/null)

if [[ -z "$TOP" ]]; then
	echo "Must be run inside the git repsitory."
	exit 1
fi

docker build -t "openzfs/jenkins-master:latest" "$TOP/docker/jenkins-master"
