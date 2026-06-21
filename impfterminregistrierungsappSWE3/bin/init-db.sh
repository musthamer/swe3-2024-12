#!/usr/bin/env bash
source local/config.txt || exit 1

echo "INIT-DB: $dbname auf Hopper"
sed -e "s/DBNAME/${dbname}/g" db/init_db.sql | mariadb -u"$dbuser" -p"$dbpassword" &&
  touch local/.db-initialized &&
  echo "INIT-DB: success" || { echo "INIT-DB: failure" >&2; exit 1; }
