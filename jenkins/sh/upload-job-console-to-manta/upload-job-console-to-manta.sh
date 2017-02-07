#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"
source "${SH_LIBRARY_PATH}/manta.sh"

check_env BUILD_URL JOB_BASE_NAME COMMIT_DIRECTORY

function get_build_result
{
    log curl -s $BUILD_URL/api/json | \
    log jq -M .result
}

function get_build_console
{
    #
    # We only want the console output from the Jenkins log. Thus, we use
    # "pup" to strip out everything else from the returned HTML, and only
    # the contents we're interested in will be included in the file.
    #
    log_must curl -s $BUILD_URL/consoleFull | \
    log_must pup --pre 'pre[class="console-output"]'
}

export HOME=$PWD
manta_setup_environment

JOB_RESULT=$(get_build_result)
while [[ -z "$JOB_RESULT" || "$JOB_RESULT" = "null" ]]; do
    sleep 1
    JOB_RESULT=$(get_build_result)
done

FILE="${JOB_BASE_NAME}.html"
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
log_must mput -f "$FILE" "/$COMMIT_DIRECTORY/$FILE"

if [[ -n "$PULL_DIRECTORY" ]]; then
    log_must mln "/$COMMIT_DIRECTORY/$FILE" "/$PULL_DIRECTORY/$FILE"
    echo "$MANTA_HTTP/$PULL_DIRECTORY/$FILE"
else
    echo "$MANTA_HTTP/$COMMIT_DIRECTORY/$FILE"
fi

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
