#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
docker run --rm -v "$PWD:/workspace:ro" -w /workspace node:24.22.0-alpine node scripts/check-release.mjs
export MVP_RELEASE_APPROVED=true
# Credentials and hostnames are supplied only by the operator's private environment file.
docker compose --env-file .env.showcase --env-file .env.mercadopago.local -f compose.showcase.yml config --quiet
docker compose --env-file .env.showcase --env-file .env.mercadopago.local -f compose.showcase.yml pull
docker compose --env-file .env.showcase --env-file .env.mercadopago.local -f compose.showcase.yml up -d --wait
