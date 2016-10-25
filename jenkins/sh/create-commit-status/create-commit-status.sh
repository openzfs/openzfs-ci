#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/vault.sh

check_env REPOSITORY COMMIT STATE CONTEXT DESCRIPTION

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

#
# We translate hyphens into spaces with the assumption that this is OK
# to do for the contexts that will be passed in; we do this because
# spaces look better in the GitHub status UI for pull requests.
#
CONTEXT=$(echo $CONTEXT | tr '-' ' ')

log_must ruby "${DIR}/${NAME}.rb" \
    --netrc-file netrc-file \
    --repository "$REPOSITORY" \
    --sha "$COMMIT" \
    --state "$STATE" \
    --context "$CONTEXT" \
    --description "$DESCRIPTION" \
    --target-url "$TARGET_URL"

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
