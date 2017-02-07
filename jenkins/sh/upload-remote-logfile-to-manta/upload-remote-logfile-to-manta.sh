#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"
source "${SH_LIBRARY_PATH}/manta.sh"

check_env OPENZFSCI_DIRECTORY DCENTER_GUEST DCENTER_HOST DCENTER_IMAGE \
    REMOTE_LOGFILE JOB_BASE_NAME COMMIT_DIRECTORY

export HOST="${DCENTER_GUEST}.${DCENTER_HOST}"
export USER=$(vault_read_ssh_user_dcenter_image $DCENTER_IMAGE)
export PASS=$(vault_read_ssh_password_dcenter_image $DCENTER_IMAGE)
export HOME=$PWD

function log_must_ssh
{
    echo "$PASS" | log_must sshpass ssh \
        -o UserKnownHostsFile=/dev/null \
        -o StrictHostKeyChecking=no \
        "$USER@$HOST" "$@"
}

manta_setup_environment

log_must test -d "$OPENZFSCI_DIRECTORY"
log_must cd "$OPENZFSCI_DIRECTORY/ansible"

log_must cat > inventory.txt <<EOF
$HOST ansible_ssh_user="$USER" ansible_ssh_pass="$PASS"
EOF

log_must cat > playbook.yml <<EOF
---
- hosts: localhost
  tasks:
    - wait_for:
        host: $HOST
        port: 22
        state: started
        timeout: 1800
EOF

log_must ansible-playbook -vvvv -i inventory.txt playbook.yml >&2

#
# While we support using shell expansion (e.g. globbing with "*") when
# evaluating REMOTE_LOGFILE, we require the expansion to evaluate to a
# single file, which is what we're attempting to verify here. If this
# evaluated to mutliple files, and we didn't have a check here, this
# would result in obscure error messages later in this script.
#
log_must_ssh "test \$(ls -1d $REMOTE_LOGFILE | wc -l) == 1"

#
# Similar to the above, we want to verify the parameter passed in is
# actually a file, to prevent obscure error messges later.
#
log_must_ssh "test -f $REMOTE_LOGFILE"

FILE="${JOB_BASE_NAME}.log"

{ log_must_ssh "cat $REMOTE_LOGFILE" | \
    pv -fi 15 2>&3 | \
    log_must mput "/$COMMIT_DIRECTORY/$FILE"; } 3>&1 | \
    stdbuf -oL -eL tr '\r' '\n' >&2

if [[ -n "$PULL_DIRECTORY" ]]; then
    log_must mln "/$COMMIT_DIRECTORY/$FILE" "/$PULL_DIRECTORY/$FILE"
    echo "$MANTA_HTTP/$PULL_DIRECTORY/$FILE"
else
    echo "$MANTA_HTTP/$COMMIT_DIRECTORY/$FILE"
fi

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
