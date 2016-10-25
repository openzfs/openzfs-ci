#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/vault.sh

check_env DCENTER_HOST DCENTER_GUEST DCENTER_SNAPSHOT

SSH='ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null'
USER=$(vault_read_ssh_user_dcenter_host $DCENTER_HOST)
PASS=$(vault_read_ssh_password_dcenter_host $DCENTER_HOST)

echo $PASS | log_must sshpass $SSH ${USER}@${DCENTER_HOST} \
    dc snapshot --no-sync $DCENTER_GUEST $DCENTER_SNAPSHOT

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
