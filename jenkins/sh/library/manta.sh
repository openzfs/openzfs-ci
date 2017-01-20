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
    log_must mfind -j -t d --mindepth 4 --maxdepth 5 "/$MANTA_USER/public" | \
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
        echo "$number" | log_must egrep -q '^[0-9]$'
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

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
