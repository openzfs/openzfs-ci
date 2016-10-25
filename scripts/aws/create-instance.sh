#!/bin/bash

set -o errexit

git-ec2-create -i ami-6e165d0e -t t2.large -d 3 -s 32 -x gp2 -k psurya-keypair
