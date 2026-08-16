#!/usr/bin/env bash
source local/config.txt || exit 1

echo "INIT-DB: $dbname auf Hopper"
sql_payload="$(sed -e "s/DBNAME/${dbname}/g" db/init_db.sql)"

# Prefer executing inside the DB container when available in local Docker setups.
if docker ps --format '{{.Names}}' | grep -qx "$dbserver"; then
  printf '%s' "$sql_payload" | docker exec -i "$dbserver" mariadb -u"$dbuser" -p"$dbpassword"
else
  printf '%s' "$sql_payload" | mariadb -h"$dbserver" -u"$dbuser" -p"$dbpassword"
fi &&
  touch local/.db-initialized &&
  echo "INIT-DB: success" || { echo "INIT-DB: failure" >&2; exit 1; }
