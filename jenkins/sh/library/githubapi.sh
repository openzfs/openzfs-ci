#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"

function githubapi_setup_environment
{
    #
    # We need to be careful not to expose the token such that it will
    # end up in the console logs of the jenkins job that will execute
    # this script.
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
}

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
