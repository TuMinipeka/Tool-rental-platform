#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
: "${BACKUP_TARGET:?Set private offsite rsync destination user@host:path}"
umask 077
mkdir -p backups
stamp=$(date -u +%Y%m%dT%H%M%SZ)
work="backups/.snapshot-$stamp"
file="backups/tool-rental-$stamp.tar"
mkdir "$work"
compose() { docker compose --env-file .env.showcase -f compose.showcase.yml "$@"; }
resume() { compose start backend >/dev/null; }
trap 'resume; rm -rf -- "$work"; rm -f -- "$file.partial"' EXIT HUP INT TERM
# A short maintenance window prevents DB/file mutations between snapshots.
compose stop backend
compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom --no-owner --no-acl' > "$work/database.dump"
test -s "$work/database.dump"
compose exec -T postgres pg_restore --list < "$work/database.dump" >/dev/null
compose run --rm --no-deps --entrypoint tar backend -C /application/storage -cf - tool-images > "$work/images.tar"
test -s "$work/images.tar"
resume
tar -C "$work" -cf "$file.partial" database.dump images.tar
mv -- "$file.partial" "$file"
rsync --protect-args -e 'ssh -o BatchMode=yes' -- "$file" "$BACKUP_TARGET"
find backups -maxdepth 1 -type f -name 'tool-rental-*.tar' -printf '%T@ %p\n' | sort -nr | tail -n +8 | cut -d' ' -f2- | while IFS= read -r old; do rm -- "$old"; done
printf 'Database and image snapshot completed; offsite copy acknowledged.\n'
