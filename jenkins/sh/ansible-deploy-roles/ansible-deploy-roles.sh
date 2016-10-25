#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/vault.sh

check_env OPENZFSCI_DIRECTORY DCENTER_HOST DCENTER_GUEST DCENTER_IMAGE \
	ROLES WAIT_FOR_SSH

#
# The Ansible roles will be contained in the "ansible" directory of the
# OpenZFS CI repository, so we need to change directories prior to
# calling an Ansible playbook that might use these roles.
#
log_must test -d "$OPENZFSCI_DIRECTORY"
log_must cd "$OPENZFSCI_DIRECTORY/ansible"

HOST="${DCENTER_GUEST}.${DCENTER_HOST}"
USER=$(vault_read_ssh_user_dcenter_image $DCENTER_IMAGE)
PASS=$(vault_read_ssh_password_dcenter_image $DCENTER_IMAGE)

log_must cat > inventory.txt <<EOF
$HOST ansible_ssh_user="$USER" ansible_ssh_pass="$PASS"
EOF

log_must cat > playbook.yml <<EOF
---
EOF

if [[ "$WAIT_FOR_SSH" == "yes" ]]; then
	log_must cat >> playbook.yml <<-EOF
	- hosts: localhost
	  tasks:
	    - wait_for:
	        host: $HOST
	        port: 22
	        state: started
	        timeout: 1800
	EOF
fi

log_must cat >> playbook.yml <<EOF
- hosts: $HOST
EOF

if [[ -n "$BECOME_USER" ]]; then
	log_must cat >> playbook.yml <<-EOF
	  become: true
	  become_user: $BECOME_USER
	EOF
fi

log_must cat >> playbook.yml <<EOF
  roles:
EOF

for ROLE in $ROLES; do
	log_must cat >> playbook.yml <<-EOF
	  - $ROLE
	EOF
done

#
# Output the contents of this file to have it logged in the Jenkins job's
# console page, making the contents more accessible which can aid debugging.
#
log_must cat playbook.yml

log_must ansible-playbook -vvvv -i inventory.txt \
	--extra-vars="$EXTRA_VARS" playbook.yml

# vim: tabstop=4 softtabstop=4 shiftwidth=4 noexpandtab textwidth=72 colorcolumn=80
