#!/bin/bash -eux

TOP=$(git rev-parse --show-toplevel 2>/dev/null)

if [[ -z "$TOP" ]]; then
	echo "Must be run inside the openzfs-ci git repsitory."
	exit 1
fi

docker build \
	-t "openzfs/jenkins-master-devel:latest" \
	"$TOP/docker/jenkins-master-devel"
