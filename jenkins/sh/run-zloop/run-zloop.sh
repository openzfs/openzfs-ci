#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh

check_env ENABLE_WATCHPOINTS RUN_TIME

DIR=$(dirname ${BASH_SOURCE[0]})

if [[ "$ENABLE_WATCHPOINTS" == "yes" ]]; then
    export ZFS_DEBUG="watch"
else
    export ZFS_DEBUG=""
fi

${DIR}/zloop.sh -t $RUN_TIME -c /var/tmp/test_results -f /var/tmp/test_results
result=$?

if [[ $result -ne 0 ]]; then
    if [[ -r ztest.cores ]]; then
        log_must cat ztest.cores
    fi

    if [[ -r core ]]; then
        log_must echo '::status' | log_must mdb core
        log_must echo '::stack' | log_must mdb core
    fi
fi

log_must tail -n 30 ztest.out

exit $result

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
