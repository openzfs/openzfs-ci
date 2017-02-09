#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"
source "${SH_LIBRARY_PATH}/manta.sh"

check_env OPENZFSCI_DIRECTORY DCENTER_GUEST DCENTER_HOST DCENTER_IMAGE \
    REMOTE_DIRECTORY JOB_BASE_NAME COMMIT_DIRECTORY

export HOME=$PWD

# These are required by the "log_must_ssh" and "wait_for_ssh" functions
export HOST="${DCENTER_GUEST}.${DCENTER_HOST}"
export USER=$(vault_read_ssh_user_dcenter_image $DCENTER_IMAGE)
export PASS=$(vault_read_ssh_password_dcenter_image $DCENTER_IMAGE)

manta_setup_environment

log_must test -d "$OPENZFSCI_DIRECTORY"
log_must cd "$OPENZFSCI_DIRECTORY/ansible"

wait_for_ssh inventory.txt playbook.yml

#
# If the directory specified via $REMOTE_DIRECTORY doesn't exist, then the
# download process below will fail. As a result, we check the existence
# of this directory, and proactively fail if it's not found.
#
log_must_ssh test -d "$REMOTE_DIRECTORY"

FILE="${JOB_BASE_NAME}.tar.xz"

{ log_must_ssh "gtar -C '$REMOTE_DIRECTORY' -cf - . | xz -9e --stdout -" | \
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
