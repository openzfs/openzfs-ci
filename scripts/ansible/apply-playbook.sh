#!/bin/bash -e

TOP=$(git rev-parse --show-toplevel 2>/dev/null)

if [[ -z "$TOP" ]]; then
	echo "Must be run inside the git repsitory."
	exit 1
fi

if [[ ! -f $TOP/ansible/venv/bin/activate ]]; then
	$TOP/scripts/ansible/virtualenv-install.sh
fi

source $TOP/ansible/venv/bin/activate
$TOP/scripts/ansible/pull-dependencies.sh

export ANSIBLE_CONFIG=$TOP/ansible/ansible.cfg
ansible-playbook -c paramiko -i $TOP/ansible/inventory $TOP/ansible/playbook.yml
