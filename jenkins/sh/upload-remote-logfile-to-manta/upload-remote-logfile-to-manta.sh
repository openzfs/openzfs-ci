#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"
source "${SH_LIBRARY_PATH}/manta.sh"

check_env OPENZFSCI_DIRECTORY DCENTER_GUEST DCENTER_HOST DCENTER_IMAGE \
    REMOTE_LOGFILE JOB_BASE_NAME COMMIT_DIRECTORY

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

manta_upload_remote_file "$REMOTE_LOGFILE" "/$COMMIT_DIRECTORY/$FILE"

if [[ -n "$PULL_DIRECTORY" ]]; then
    log_must mln "/$COMMIT_DIRECTORY/$FILE" "/$PULL_DIRECTORY/$FILE"
    echo "$MANTA_HTTP/$PULL_DIRECTORY/$FILE"
else
    echo "$MANTA_HTTP/$COMMIT_DIRECTORY/$FILE"
fi

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
