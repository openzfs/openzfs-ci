#!/bin/bash

#
# We want the "die" function to kill the entire job, not just the
# current shell; "exit 1" by itself will not work in cases were we start
# a subshell such as:
#
# var=$(command_invoked_in_subshell arg1 arg2)
#
# So we set a signal handler for SIGTERM and remember the pid of the top
# level process so that the die function can always send it SIGTERM.
#
export __OPENZFSCI_DIE_TOP_PID=$$
trap "exit 1" TERM

function die
{
    local bold="\E[1;31m"
    local norm="\E[0m"

    local msg=$1
    echo -e "${bold}failed:${norm} $msg" >&2
    kill -s TERM $__OPENZFSCI_DIE_TOP_PID
    exit 1
}

function log
{
    local bold="\E[1m"
    local norm="\E[0m"

    local args=()
    for arg in "$@"; do
        args[${#args[*]}]="$arg"
    done

    echo -e "${bold}running:${norm} ${args[*]}" >&2
    "${args[@]}"
    return $?
}

function log_must
{
    local args=()
    for arg in "$@"; do
        args[${#args[*]}]="$arg"
    done

    log "${args[@]}" || die "${args[*]}"
    return 0
}

function check_env
{
    for variable in "$@"; do
        eval value=\$$variable
        if [[ -z "$value" ]]; then
            die "$variable must be non-empty"
        fi
    done
    return 0
}

function log_must_ssh
{
    #
    # It's arguable whether it's "good" to use "magic environment
    # variables" to pass information into this function, vs. function
    # parameters; for now, use environment variables since the consumers
    # already expect this behavior.
    #
    check_env HOST USER PASS

    #
    # We need to be careful not to use "log_must" with "echo" below, as
    # that could leak the password (print to whatever happened to be
    # capturing the output of this function; e.g. a Jenkins job log).
    #
    echo "$PASS" | log_must sshpass ssh \
        -o UserKnownHostsFile=/dev/null \
        -o StrictHostKeyChecking=no \
        "$USER@$HOST" "$@"
}

function wait_for_ssh
{
    local INVENTORY="$1"
    local PLAYBOOK="$2"

    #
    # Again, just like with "log_mush_ssh" above; perhaps these
    # shouldn't be environment variables, but for now, this is the
    # simplest/fastest way to retro-fit the existing consumers.
    #
    check_env HOST USER PASS INVENTORY PLAYBOOK

    #
    # The tabs below aren't a mistake, that's a requirement of using a
    # heredoc with indentation.
    #

    log_must cat > "$INVENTORY" <<-EOF
	$HOST ansible_ssh_user="$USER" ansible_ssh_pass="$PASS"
	EOF

    log_must cat > "$PLAYBOOK" <<-EOF
	---
	- hosts: localhost
	  tasks:
	    - wait_for:
	        host: $HOST
	        port: 22
	        state: started
	        timeout: 1800
	EOF

    log_must ansible-playbook -vvvv -i "$INVENTORY" "$PLAYBOOK" >&2
}

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
