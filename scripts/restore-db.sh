#!/bin/sh
set -eu

backup=${1:?Usage: CONFIRM_RESTORE=nearkart ./scripts/restore-db.sh /path/nearkart.dump}
[ -f "$backup" ] || { echo "Backup file not found: $backup" >&2; exit 1; }
[ "${CONFIRM_RESTORE:-}" = nearkart ] || { echo "Set CONFIRM_RESTORE=nearkart to replace the target database" >&2; exit 1; }

if [ -n "${DATABASE_URL:-}" ]; then
	command -v pg_restore >/dev/null 2>&1 || { echo "pg_restore is required" >&2; exit 1; }
	pg_restore --clean --if-exists --no-owner --exit-on-error --dbname="$DATABASE_URL" "$backup"
else
	root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
	docker compose -f "$root/docker-compose.yml" exec -T postgres pg_restore --clean --if-exists \
		--no-owner --exit-on-error -U "${POSTGRES_USER:-nearkart}" -d "${POSTGRES_DB:-nearkart}" < "$backup"
fi

echo "Database restore completed. Restart the API and run ./scripts/api-smoke.sh."
