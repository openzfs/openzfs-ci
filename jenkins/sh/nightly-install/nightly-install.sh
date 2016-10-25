#!/bin/bash

set -o nounset
set -o errexit

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/os-nightly-lib.sh

check_env OPENZFS_DIRECTORY INSTALL_DEBUG

OPENZFS_DIRECTORY=$(log_must readlink -f "$OPENZFS_DIRECTORY")
log_must test -d "$OPENZFS_DIRECTORY"
log_must cd "$OPENZFS_DIRECTORY"

ONU="${OPENZFS_DIRECTORY}/usr/src/tools/scripts/onu"
REPO="${OPENZFS_DIRECTORY}/packages/i386/nightly"
[[ "$INSTALL_DEBUG" = "yes" ]] || REPO="${REPO}-nd"

log_must sudo "${ONU}" -t "openzfs-nightly" -d "${REPO}"

exit 0

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
