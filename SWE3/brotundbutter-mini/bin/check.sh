#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"
curl -s "$path/hello"
