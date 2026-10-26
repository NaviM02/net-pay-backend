#!/usr/bin/env bash
set -euo pipefail
[[ $EUID -eq 0 ]] || { echo 'Run through the SSM deploy document as root.' >&2; exit 1; }
release=${1:?Pass a 40-character Git commit SHA}
[[ $release =~ ^[a-f0-9]{40}$ ]] || { echo 'invalid_release' >&2; exit 2; }

config=/etc/netpay/deploy.env
old=$(awk -F= '$1 == "APP_IMAGE_TAG" {print $2}' "$config" || true)
temporary=$(mktemp /etc/netpay/deploy.env.XXXXXX)
trap 'rm -f -- "$temporary"' EXIT
awk -F= '$1 != "APP_IMAGE_TAG"' "$config" >"$temporary"
printf 'APP_IMAGE_TAG=%s\n' "$release" >>"$temporary"
chmod 600 "$temporary"
mv "$temporary" "$config"
trap - EXIT

if ! systemctl restart netpay.service; then
  echo 'Deployment unhealthy; inspect journalctl -u netpay.service.' >&2
  if [[ $old =~ ^[a-f0-9]{40}$ ]]; then
    temporary=$(mktemp /etc/netpay/deploy.env.XXXXXX)
    trap 'rm -f -- "$temporary"' EXIT
    awk -F= '$1 != "APP_IMAGE_TAG"' "$config" >"$temporary"
    printf 'APP_IMAGE_TAG=%s\n' "$old" >>"$temporary"
    chmod 600 "$temporary"
    mv "$temporary" "$config"
    trap - EXIT
    # This restores the previous binary only. It does not reverse Flyway migrations.
    systemctl restart netpay.service || true
  fi
  exit 1
fi
