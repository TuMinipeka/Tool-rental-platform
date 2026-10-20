#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
: "${EVALUATOR_EMAIL:?Set evaluator.name email}"
: "${EVALUATOR_PASSWORD:?Set private password of 12 to 72 characters}"
: "${EVALUATOR_ROLE:?Set CUSTOMER, SUPPLIER or ADMIN}"
export EVALUATOR_EMAIL EVALUATOR_PASSWORD EVALUATOR_ROLE
docker compose --env-file .env.showcase --env-file .env.mercadopago.local -f compose.showcase.yml run --rm --no-deps -e EVALUATOR_EMAIL -e EVALUATOR_PASSWORD -e EVALUATOR_ROLE --entrypoint java backend -cp 'infrastructure-0.1.0-SNAPSHOT.jar:lib/*' com.toolrental.infrastructure.common.bootstrap.EvaluatorProvisioner
