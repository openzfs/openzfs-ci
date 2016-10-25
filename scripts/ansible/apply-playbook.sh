#!/bin/bash

TOP=$(git rev-parse --show-toplevel 2>/dev/null)

if [[ -z "$TOP" ]]; then
	echo "Must be run inside the openzfs-ci git repsitory."
	exit 1
fi

if [[ ! -f $TOP/ansible/venv/bin/activate ]]; then
	$TOP/scripts/ansible/virtualenv-install.sh
fi

source $TOP/ansible/venv/bin/activate

$TOP/scripts/ansible/pull-dependencies.sh

ansible-playbook -i $TOP/ansible/inventory/production $TOP/ansible/playbook.yml

