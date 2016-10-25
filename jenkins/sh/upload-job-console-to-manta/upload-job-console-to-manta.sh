#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/vault.sh

check_env JENKINS_URL JOB_NAME BUILD_NUMBER REPOSITORY COMMIT

function get_build_result
{
    log curl -s $JENKINS_URL/job/$JOB_NAME/$BUILD_NUMBER/api/json | \
    log jq -M .result
}

function get_build_console
{
    #
    # We only want the console output from the Jenkins log. Thus, we use
    # "pup" to strip out everything else from the returned HTML, and only
    # the contents we're interested in will be included in the file.
    #
    log_must curl -s $JENKINS_URL/job/$JOB_NAME/$BUILD_NUMBER/consoleFull | \
    log_must pup --pre 'pre[class="console-output"]'
}

export MANTA_URL=$(vault_read_manta_url)
export MANTA_USER=$(vault_read_manta_user)
export MANTA_KEY_ID=$(vault_read_manta_keyid)

export HOME=$PWD

log_must mkdir -p $HOME/.ssh
log_must chmod 700 $HOME/.ssh

log_must vault_read_manta_private_key > $HOME/.ssh/id_rsa
log_must chmod 400 $HOME/.ssh/id_rsa

log_must vault_read_manta_public_key > $HOME/.ssh/id_rsa.pub
log_must chmod 644 $HOME/.ssh/id_rsa

JOB_RESULT=$(get_build_result)
while [[ -z "$JOB_RESULT" || "$JOB_RESULT" = "null" ]]; do
    sleep 1
    JOB_RESULT=$(get_build_result)
done

FILE="${JOB_NAME}.html"
get_build_console > "$FILE"

#
# The "pup" command will return a successful error code even if it
# doesn't find a match. Thus, when this happens, we want to catch and
# report the issue as soon as possible, as the file that is intended to
# contain the console output will actually be an empty file. Since we
# can't rely on the exit code of "pup", we have to explicitly check the
# size of the file after it is created.
#
log_must test -s "$FILE"

COMMITS_DIRECTORY="$MANTA_USER/public/$REPOSITORY/commit/$COMMIT"
log_must mmkdir -p "/$COMMITS_DIRECTORY"
log_must mput -f "$FILE" "/$COMMITS_DIRECTORY/$FILE"

if [[ -n "$PULL_NUMBER" ]]; then
    PULLS_DIRECTORY="$MANTA_USER/public/$REPOSITORY/pull/$PULL_NUMBER"
    log_must mmkdir -p "/$PULLS_DIRECTORY"
    log_must mln "/$COMMITS_DIRECTORY/$FILE" "/$PULLS_DIRECTORY/$FILE"

    echo "$MANTA_URL/$PULLS_DIRECTORY/$FILE"
else
    echo "$MANTA_URL/$COMMITS_DIRECTORY/$FILE"
fi

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
