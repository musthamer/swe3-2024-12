#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"

wait_for_success() {
	local cmd="$1"
	local label="$2"
	local retries=30
	local delay=2
	local i=1

	while [ "$i" -le "$retries" ]; do
		if eval "$cmd"; then
			return 0
		fi
		sleep "$delay"
		i=$((i + 1))
	done

	echo "CHECK: $label failure" >&2
	return 1
}

echo "CHECK: assembled.txt"
wait_for_success "curl -sf --connect-timeout 2 --max-time 3 '$path/assembled.txt' >/dev/null" "assembled.txt" || exit 1

echo "CHECK: index.html"
wait_for_success "curl -sf --connect-timeout 2 --max-time 3 -o /dev/null '$path/index.html'" "index.html" || exit 1

echo "CHECK: api/vaccination-centers"
wait_for_success "curl -sf --connect-timeout 2 --max-time 3 '$path/api/vaccination-centers' | grep -q '\"success\":true'" "api" || exit 1

echo "CHECK: emails (redis)"
wait_for_success "curl -sf --connect-timeout 2 --max-time 3 -o /dev/null '$path/emails'" "emails" || exit 1

echo "CHECK: success – $path/"
