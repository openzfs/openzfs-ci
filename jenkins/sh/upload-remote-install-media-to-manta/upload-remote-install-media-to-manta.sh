#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"
source "${SH_LIBRARY_PATH}/manta.sh"

check_env OPENZFSCI_DIRECTORY DCENTER_HOST DCENTER_GUEST DCENTER_IMAGE \
    REMOTE_DIRECTORY PREFIX REPOSITORY BRANCH

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

MANTA_DIRECTORY="/$MANTA_USER/public/$PREFIX/$REPOSITORY/$BRANCH"
log_must mmkdir -p "$MANTA_DIRECTORY"
manta_upload_remote_directory \
    "$REMOTE_DIRECTORY" "$MANTA_DIRECTORY/install-media.tar.xz"

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
