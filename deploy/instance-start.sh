#!/usr/bin/env bash
set -euo pipefail

CONFIG=/etc/netpay/deploy.env
[[ -f "$CONFIG" ]] || exit 1
# This root-owned file is generated only from validated Terraform variables.
# shellcheck disable=SC1090
source "$CONFIG"
[[ -n ${APP_IMAGE_TAG:-} ]] || exit 0 # First boot before the first CI deploy.
[[ $APP_IMAGE_TAG =~ ^[a-f0-9]{40}$ ]] || { echo 'invalid_image_tag' >&2; exit 2; }

install -d -m 700 /run/netpay
aws ssm get-parameter --region "$AWS_REGION" --name "$RUNTIME_PARAMETER" --with-decryption \
  --query Parameter.Value --output text | /usr/local/bin/netpay-write-env

export DOCKER_CONFIG
DOCKER_CONFIG=$(mktemp -d /run/netpay/docker.XXXXXX)
trap 'rm -rf -- "$DOCKER_CONFIG"' EXIT
registry=${ECR_REPOSITORY%%/*}
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$registry" >/dev/null

compose=(docker compose --project-directory /etc/netpay --env-file "$CONFIG" -f /etc/netpay/compose.prod.yml)
"${compose[@]}" config --quiet
"${compose[@]}" pull app
"${compose[@]}" up -d --wait --wait-timeout 300
curl --fail --silent --show-error --connect-timeout 5 --max-time 15 \
  --retry 12 --retry-delay 5 --retry-all-errors "https://$API_HOST/health" >/dev/null
echo "NetPay healthy at image $APP_IMAGE_TAG"
