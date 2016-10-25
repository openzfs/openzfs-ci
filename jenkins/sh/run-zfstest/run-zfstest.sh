#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh

check_env RUNFILE

DIR=$(dirname ${BASH_SOURCE[0]})

log_must sudo sed -i "s/timeout = .*/timeout = 10800/" $RUNFILE

log_must ppriv -s EIP=basic -e \
    /opt/zfs-tests/bin/zfstest -a -c $RUNFILE 2>&1 | tee results.txt

log_must ${DIR}/zfstest-report.py results.txt

exit 0

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
