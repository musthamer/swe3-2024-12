#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.." || exit 1

[ -f local/config.txt ] || bin/configure.sh
[ -d lib ] && [ -n "$(ls -A lib 2>/dev/null)" ] || bin/download-libs.sh
[ -f local/.db-initialized ] || bin/init-db.sh

bin/clean.sh &&
bin/prepare.sh &&
bin/compile.sh &&
bin/assemble.sh &&
bin/deploy.sh &&
bin/check.sh
