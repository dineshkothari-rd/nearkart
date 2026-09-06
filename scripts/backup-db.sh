#!/bin/sh
set -eu
umask 077

output=${1:?Usage: ./scripts/backup-db.sh /existing/directory/nearkart.dump}
[ -d "$(dirname "$output")" ] || { echo "Output directory does not exist" >&2; exit 1; }
temporary="$output.tmp.$$"
trap 'rm -f "$temporary"' EXIT INT TERM

if [ -n "${DATABASE_URL:-}" ]; then
	command -v pg_dump >/dev/null 2>&1 || { echo "pg_dump is required" >&2; exit 1; }
	pg_dump --format=custom --no-owner --file="$temporary" "$DATABASE_URL"
else
	root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
	docker compose -f "$root/docker-compose.yml" exec -T postgres pg_dump --format=custom --no-owner \
		-U "${POSTGRES_USER:-nearkart}" -d "${POSTGRES_DB:-nearkart}" > "$temporary"
fi

[ -s "$temporary" ] || { echo "Backup is empty" >&2; exit 1; }
mv "$temporary" "$output"
trap - EXIT INT TERM
echo "Backup written to $output"
