#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"

check_env OPENZFSCI_DIRECTORY DCENTER_GUEST DCENTER_HOST DCENTER_IMAGE \
    REMOTE_DIRECTORY JOB_NAME REPOSITORY COMMIT

export HOST="${DCENTER_GUEST}.${DCENTER_HOST}"
export USER=$(vault_read_ssh_user_dcenter_image $DCENTER_IMAGE)
export PASS=$(vault_read_ssh_password_dcenter_image $DCENTER_IMAGE)

export MANTA_URL=$(vault_read_manta_url)
export MANTA_USER=$(vault_read_manta_user)
export MANTA_KEY_ID=$(vault_read_manta_keyid)

export HOME=$PWD

function log_must_ssh
{
    echo "$PASS" | log_must sshpass ssh \
        -o UserKnownHostsFile=/dev/null \
        -o StrictHostKeyChecking=no \
        "$USER@$HOST" "$@"
}

log_must mkdir -p $HOME/.ssh
log_must chmod 700 $HOME/.ssh

log_must vault_read_manta_private_key > $HOME/.ssh/id_rsa
log_must chmod 400 $HOME/.ssh/id_rsa

log_must vault_read_manta_public_key > $HOME/.ssh/id_rsa.pub
log_must chmod 644 $HOME/.ssh/id_rsa

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
# If the directory specified via $REMOTE_DIRECTORY doesn't exist, then the
# download process below will fail. As a result, we check the existence
# of this directory, and proactively fail if it's not found.
#
log_must_ssh test -d "$REMOTE_DIRECTORY"

COMMITS_DIRECTORY="$MANTA_USER/public/$REPOSITORY/commit/$COMMIT"
FILE="${JOB_NAME}.tar.xz"

log_must mmkdir -p "/$COMMITS_DIRECTORY"

{ log_must_ssh "gtar -C '$REMOTE_DIRECTORY' -cf - . | xz -9e --stdout -" | \
    pv -fi 15 2>&3 | \
    log_must mput "/$COMMITS_DIRECTORY/$FILE"; } 3>&1 | \
    stdbuf -oL -eL tr '\r' '\n' >&2

if [[ -n "$PULL_NUMBER" ]]; then
    PULLS_DIRECTORY="$MANTA_USER/public/$REPOSITORY/pull/$PULL_NUMBER"
    log_must mmkdir -p "/$PULLS_DIRECTORY"
    log_must mln "/$COMMITS_DIRECTORY/$FILE" "/$PULLS_DIRECTORY/$FILE"

    echo "$MANTA_URL/$PULLS_DIRECTORY/$FILE"
else
    echo "$MANTA_URL/$COMMITS_DIRECTORY/$FILE"
fi

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
