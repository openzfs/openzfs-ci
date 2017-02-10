#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"
source "${SH_LIBRARY_PATH}/manta.sh"

check_env PREFIX REPOSITORY PULL_NUMBER

export HOME=$PWD
manta_setup_environment

DIRECTORY="$MANTA_USER/public/$PREFIX/$REPOSITORY/pull/$PULL_NUMBER"
NUMBER=$(manta_get_next_number_for_directory "$DIRECTORY")

log_must mmkdir -p "/$DIRECTORY/$NUMBER"
echo "$DIRECTORY/$NUMBER"

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
