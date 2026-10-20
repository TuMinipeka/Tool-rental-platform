#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
: "${BACKUP_TARGET:?Set private offsite rsync destination user@host:path}"
umask 077
mkdir -p backups
stamp=$(date -u +%Y%m%dT%H%M%SZ)
file="backups/tool-rental-$stamp.dump"
trap 'rm -f -- "$file.partial"' EXIT HUP INT TERM
docker compose --env-file .env.showcase -f compose.showcase.yml exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom --no-owner --no-acl' > "$file.partial"
test -s "$file.partial"
docker compose --env-file .env.showcase -f compose.showcase.yml exec -T postgres pg_restore --list < "$file.partial" > /dev/null
mv -- "$file.partial" "$file"
# Copy offsite before pruning local history. SSH keys stay outside the repository.
rsync --protect-args -e 'ssh -o BatchMode=yes' -- "$file" "$BACKUP_TARGET"
find backups -maxdepth 1 -type f -name 'tool-rental-*.dump' -printf '%T@ %p\n' | sort -nr | tail -n +8 | cut -d' ' -f2- | while IFS= read -r old; do rm -- "$old"; done
printf 'Backup completed; offsite copy acknowledged.\n'
