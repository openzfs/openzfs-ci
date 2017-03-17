#!/bin/bash -e

if [[ $EUID -ne 0 ]]; then
	echo "This script must be run as root." 1>&2
	exit 1
fi

apt-get update
apt-get install -y python-pip python-dev libffi-dev libssl-dev \
	libxml2-dev libxslt1-dev libjpeg8-dev zlib1g-dev

pip install virtualenv
