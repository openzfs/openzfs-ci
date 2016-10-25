#!/bin/bash

set -o nounset
set -o errexit

source ${SH_LIBRARY_PATH}/common.sh

check_env OPENZFS_DIRECTORY

#
# The illumos build cannot be run as the root user. The Jenkins
# infrastructure built around running the build should ensure the build
# is not attempted as root. In case that fails for whatever reason, it's
# best to fail early and with a good error message, than failing later
# with an obscure build error.
#
[ $EUID -ne 0 ] || die "nits attempted as root user; this is not supported."

OPENZFS_DIRECTORY=$(log_must readlink -f "$OPENZFS_DIRECTORY")
log_must test -d "$OPENZFS_DIRECTORY"
log_must cd "$OPENZFS_DIRECTORY"

#
# The assumption is this script will run after a full nightly build of
# illumos, and the environment file will already exist as a byproduct of
# the build. If this is not the case, it's best to explicitly fail here.
#
log_must test -f "illumos.sh"

log_must ln -sf usr/src/tools/scripts/bldenv.sh .

BASE=${BASE_COMMIT:-'HEAD^'}
log env -i ksh93 bldenv.sh -d "illumos.sh" -c "git nits -b '$BASE'" 2>&1

exit 0

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
