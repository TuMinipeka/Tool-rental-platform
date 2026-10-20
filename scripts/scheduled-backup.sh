#!/usr/bin/env sh
set -eu
config=${BACKUP_CONFIG:-/etc/toolrental/backup.env}
test -r "$config" || { logger -t tool-rental-backup 'Private backup configuration unavailable'; exit 1; }
set -a
. "$config"
set +a
root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
if "$root/scripts/backup.sh"; then
    logger -t tool-rental-backup 'Daily backup and offsite transfer completed'
else
    logger -t tool-rental-backup 'Daily backup failed; inspect private operator logs'
    exit 1
fi
