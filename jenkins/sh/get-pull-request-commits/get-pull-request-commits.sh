#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/vault.sh

check_env REPOSITORY PULL_REQUEST_VARIANT

DIR=$(dirname ${BASH_SOURCE[0]})
NAME=$(basename -s ".sh" ${BASH_SOURCE[0]})

#
# We need to be careful not to expose the token such that it will end up
# in the console logs of the jenkins job that will execute this script.
#
log_must cat > netrc-file <<EOF
machine api.github.com
  login $(vault_read_github_user)
  password $(vault_read_github_token)
EOF

#
# The ruby netrc module will throw an error if the netrc file's
# permissions are not 600.
#
log_must chmod 600 netrc-file

log_must mkdir -p "$REPOSITORY"

log_must ruby "${DIR}/${NAME}.rb" \
    --pull-requests-file "$REPOSITORY/pull-requests.json" \
    --netrc-file netrc-file \
    --repository "$REPOSITORY" \
    --pull-request-variant "$PULL_REQUEST_VARIANT"

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
