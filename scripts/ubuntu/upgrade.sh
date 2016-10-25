#!/bin/bash

set -o errexit
set -o nounset

if [[ "$EUID" -ne "0" ]]; then
  echo "ERROR: Must be run as root." >&2
  exit 1
fi

apt-get update
apt-get upgrade
apt-get dist-upgrade
apt-get install update-manager-core
do-release-upgrade
