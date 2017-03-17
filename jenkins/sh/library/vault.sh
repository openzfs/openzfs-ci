#!/bin/bash

source "${SH_LIBRARY_PATH}/common.sh"

function vault_setup_environment
{
    #
    # This has a lot of assumptions about the environment that it is
    # running in; it assumes this function will be running inside of a
    # Docker contianer, and the Docker host will be running the
    # Hashicorp Vault service. Thus, we get the Docker host's IP address
    # by inspecting this containers default route, which will be the
    # Docker host. Then we can configure the VAULT_ADDR environment
    # variable to point back to the Docker host that's running the
    # service.
    #
    local address=$(ip route | awk '/default/ { print $3 }')

    export VAULT_ADDR="http://${address}:8200"
    export VAULT_TOKEN="14183ec4-a7f3-10b6-232a-d9f9d63928dc"
}

function vault_read_smtp_user
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/smtp/user
}

function vault_read_smtp_password
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/smtp/password
}

function vault_read_github_user
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/github/user
}

function vault_read_github_token
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/github/token
}

function vault_read_github_public_key
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/github/public-key
}

function vault_read_github_private_key
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/github/private-key
}

function verify_dcenter_host
{
    [[ -n "$1" && "$1" == "dcenter" ]] || \
        die "invalid dcenter host: $1"
}

function vault_read_ssh_user_dcenter_host
{
    verify_dcenter_host "$1"
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/dcenter-hosts/$1/user
}

function vault_read_ssh_password_dcenter_host
{
    verify_dcenter_host "$1"
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/dcenter-hosts/$1/password
}

function verify_dcenter_image
{
    [[ -n "$1" ]] || die "dcenter image not specified"
    [[ "$1" == "omnios-r151020" ]] || [[ "$1" == "oi-hipster" ]] || \
        die "invalid dcenter image specified: $1"
}

function vault_read_ssh_user_dcenter_image
{
    verify_dcenter_image "$1"
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/dcenter-images/$1/user
}

function vault_read_ssh_password_dcenter_image
{
    verify_dcenter_image "$1"
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/dcenter-images/$1/password
}

function vault_read_manta_https
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/manta/https
}

function vault_read_manta_http
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/manta/http
}

function vault_read_manta_user
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/manta/user
}

function vault_read_manta_keyid
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/manta/keyid
}

function vault_read_manta_private_key
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/manta/private-key
}

function vault_read_manta_public_key
{
    [[ -z "$VAULT_TOKEN" ]] && vault_setup_environment
    vault read -field=value secret/openzfsci/manta/public-key
}

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
