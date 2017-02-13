#!/bin/bash

source ${SH_LIBRARY_PATH}/common.sh
source ${SH_LIBRARY_PATH}/vault.sh

export IMAGE_ID="ami-4ba9625d"
export INSTANCE_TYPE="m4.xlarge"

export HOME=$PWD

log_must mkdir -p $HOME/.aws
log_must cat > $HOME/.aws/credentials <<EOF
[default]
aws_access_key_id = $(vault_read_aws_access_key)
aws_secret_access_key = $(vault_read_aws_secret_key)
region = $(vault_read_aws_region)
EOF

INSTANCE_ID=$(log_must aws ec2 run-instances \
    --image-id $IMAGE_ID \
    --count 1 \
    --instance-type $INSTANCE_TYPE \
    --associate-public-ip-address | jq -M -r .Instances[0].InstanceId)

log_must echo "$INSTANCE_ID"

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
