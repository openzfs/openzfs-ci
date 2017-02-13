#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/vault.sh

check_env INSTANCE_IDS

export HOME=$PWD

log_must mkdir -p $HOME/.aws
log_must cat > $HOME/.aws/credentials <<EOF
[default]
aws_access_key_id = $(vault_read_aws_access_key)
aws_secret_access_key = $(vault_read_aws_secret_key)
region = $(vault_read_aws_region)
EOF

log_must aws ec2 terminate-instances --instance-ids "$INSTANCE_IDS"

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
