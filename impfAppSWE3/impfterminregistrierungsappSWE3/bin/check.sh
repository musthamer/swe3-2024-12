#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"

echo "CHECK: assembled.txt"
curl -sf "$path/assembled.txt" || { echo "CHECK: assembled.txt failure" >&2; exit 1; }

echo "CHECK: index.html"
curl -sf -o /dev/null "$path/index.html" || { echo "CHECK: index.html failure" >&2; exit 1; }

echo "CHECK: api/vaccination-centers"
curl -sf "$path/api/vaccination-centers" | grep -q '"success":true' || { echo "CHECK: api failure" >&2; exit 1; }

echo "CHECK: emails (redis)"
curl -sf -o /dev/null "$path/emails" || { echo "CHECK: emails failure" >&2; exit 1; }

echo "CHECK: success – $path/"
