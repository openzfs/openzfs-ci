#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"
source "${SH_LIBRARY_PATH}/githubapi.sh"

check_env REPOSITORY

DIR=$(dirname ${BASH_SOURCE[0]})
NAME=$(basename -s ".sh" ${BASH_SOURCE[0]})

githubapi_setup_environment

#
# To avoid the password being present in the Jenkins job console page,
# we pass the SMTP password to the ruby script via the processes stdin.
#
echo $(vault_read_smtp_password) | log_must ruby "${DIR}/${NAME}.rb" \
    --netrc-file netrc-file \
    --repository "$REPOSITORY" \
    --smtp-user "$(vault_read_smtp_user)" \
    --smtp-password "-"

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
