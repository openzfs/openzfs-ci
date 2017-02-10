#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/vault.sh"

function manta_setup_environment
{
    check_env HOME

    export MANTA_URL=$(vault_read_manta_https)
    export MANTA_HTTP=$(vault_read_manta_http)
    export MANTA_USER=$(vault_read_manta_user)
    export MANTA_KEY_ID=$(vault_read_manta_keyid)

    log_must mkdir -p $HOME/.ssh
    log_must chmod 700 $HOME/.ssh

    log_must vault_read_manta_private_key > $HOME/.ssh/id_rsa
    log_must chmod 400 $HOME/.ssh/id_rsa

    log_must vault_read_manta_public_key > $HOME/.ssh/id_rsa.pub
    log_must chmod 644 $HOME/.ssh/id_rsa.pub
}

function manta_get_current_number_for_directory
{
    check_env MANTA_USER

    [[ -n "$1" ]] || die "directory not specified"

    #
    # We specificaly do not use "mls $1" or "mfind -t d $1" because both
    # of these commands will fail if the directory they're operating on
    # doesn't already exist on Manta (i.e. if "$1" doesn't exist). When
    # testing a pull request or commit for the first time, this
    # directory won't yet exist, since it's first generated after this
    # function is used.
    #
    # If we simply removed "log_must" when executing "mfind", that would
    # also address the issue, but then we would interpret all failures
    # of "mfind" as the directory not existing. This could result in
    # data corruption, as the logic depending on this function would act
    # as if the directory doesn't already exist, when in fact it does.
    #
    # To work around this issue, we execute "mfind" on a directory that
    # is guaranteed to exist, and then use mfind's "--mindepth" and
    # "--maxdepth" options, coupled with jq's "select" operator, to
    # filter the results to only the specific parent directory and
    # sub-directories that we care about.
    #
    log_must mfind -j -t d --mindepth 5 --maxdepth 6 "/$MANTA_USER/public" | \
    log_must jq -M -r "select( .parent == \"/$1\" ) | .name" | \
    log_must sort -n | \
    log_must tail -n1

}

#
# WARNING: This function is inherently racy. The logic used to get the
# "current" number for the directory, and then increment it to get the
# "next" number for the directory occurs without any locking across the
# directory. Thus, if two threads/processes happen to concurrently query
# for the "next" number for any given directory, it's possible for each
# to receive the same value back, each thinking that they have exclusive
# access to that directory number.
#
# Thus, this function should only be used when that race condition isn't
# possible due to some other restrictions placed on the code that would
# call into this function, or the existence of this race condition is
# acceptable (e.g. because the chance of it occurring in practice is
# "small enough", or the impact of it occuring is "small enough").
#
function manta_get_next_number_for_directory
{
    check_env MANTA_USER

    [[ -n "$1" ]] || die "directory not specified"

    local number=$(manta_get_current_number_for_directory "$1")

    if [[ -n "$number" ]]; then
        #
        # To be defensive, we double check that the value returned by
        # the "get_directory_number" function is an integer.
        #
        echo "$number" | log_must egrep -q '^[0-9]+$'
    else
        #
        # If the "get_directory_number" function returns the empty
        # string, this means there haven't been any previous test
        # iterations for the given directory. In that case, we assign
        # "0" here, such that it will be incremented to "1" below, and
        # the numbers for this directory will start at "1".
        #
        number="0"
    fi

    echo $((number + 1))
}

function manta_upload_remote_directory
{
    local REMOTE_DIRECTORY="$1"
    local MANTA_FILE="$2"

    check_env HOST USER PASS REMOTE_DIRECTORY MANTA_FILE

    #
    # We inject the pv(1) (pipe viewer) utility in between the stream so
    # we can output statistics about the speed of the transfer.
    #
    # By default, pv(1) will print carriage returns instead of newline
    # characters each time it updates the statistics it's showing. It
    # does this so that it can continually update the output inline,
    # instead of printing a new line for each update, when it is run
    # interactively from the command line.
    #
    # Unfortunately, Jenkins does not handle carriage returns the same
    # way it does new lines. The "console" page for a job will only be
    # updated when a newline is printed. Thus, as pv(1) sends updates,
    # these will not get propagated to the job's "console" page until
    # the stream completes, and a new line is printed. This essentially
    # defeats the purpose of using pv(1) at all, since the point is to
    # periodically display output as feedback to let the user know the
    # transfer is progressing.
    #
    # To work around this limitation, we pipe the output from pv(1) into
    # tr(1) and replace any carriage returns with newlines; this will
    # achieve the desired behavior with each update from pv(1) being
    # immediately displayed on a new line in the Jenkins job's "console"
    # page.

    #
    # Additionally, we can't simply pipe the output from pv(1) (which is
    # printed on stderr) directly, since we're already using a pipe to
    # transfer the data from the remote host to "mput". If we simply
    # redirected the stderr from pv(1) to stdout, that output would end
    # up getting incorrectly consumed by "mput". As a result, we have to
    # use groups (the "{" and "}") and complicated redirection to tease
    # the output from pv(1), and pipe only that data into tr(1); leaving
    # the data from the remote host alone, so that it can get passed to
    # "mput" as we need.
    #
    # Thus, we redirect the stderr from the pv(1) command to FD 3. Then,
    # outside of the group and after the data from the remote host has
    # been consumed by "mput", we redirect the data from pv(1) back to
    # stdout, such that it can be piped into tr(1) and printed to the
    # Jenkins "console" page.
    #
    # The last remaining bit to explain is the usage of stdbuf(1). We
    # need to use this to prevent bash from buffering the output of
    # tr(1). Without this command, the lines from pv(1) won't get
    # immediately printed to the Jenkins job's "console" page. Instead,
    # multiple lines would get batched up and all printed at the same
    # time, instead of each line being printed to the "console" page as
    # they are emitted from pv(1). By using stdbuf(1), we prevent this
    # buffering/batching of multiple lines from occurring.
    #
    { log_must_ssh "gtar -C '$REMOTE_DIRECTORY' -cf - . | xz -9e --stdout -" | \
        pv -fi 15 2>&3 | \
        log_must mput "$MANTA_FILE"; } 3>&1 | \
        stdbuf -oL -eL tr '\r' '\n' >&2
}

function manta_upload_remote_file
{
    local REMOTE_FILE="$1"
    local MANTA_FILE="$2"

    check_env HOST USER PASS REMOTE_FILE MANTA_FILE

    #
    # See the comment in "manta_upload_remote_directory" above for an
    # explanation of the complexity here.
    #
    { log_must_ssh "cat $REMOTE_FILE" | \
        pv -fi 15 2>&3 | \
        log_must mput "$MANTA_FILE"; } 3>&1 | \
        stdbuf -oL -eL tr '\r' '\n' >&2
}

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
