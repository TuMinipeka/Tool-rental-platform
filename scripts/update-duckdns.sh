#!/usr/bin/env sh
set -eu
: "${DUCKDNS_SUBDOMAIN:?Set registered DuckDNS subdomain without suffix}"
: "${DUCKDNS_TOKEN:?Set DuckDNS private account token}"
: "${PUBLIC_IPV4:?Set the reserved public server IPv4}"
case "$DUCKDNS_SUBDOMAIN" in *[!a-z0-9-]*|'') echo 'Invalid subdomain' >&2; exit 1;; esac
case "$DUCKDNS_TOKEN" in *[!a-zA-Z0-9-]*|'') echo 'Invalid token format' >&2; exit 1;; esac
case "$PUBLIC_IPV4" in *[!0-9.]*|'') echo 'Invalid address format' >&2; exit 1;; esac
# The token travels over stdin, never as a URL or command argument.
result=$(printf 'domains=%s&token=%s&ip=%s' "$DUCKDNS_SUBDOMAIN" "$DUCKDNS_TOKEN" "$PUBLIC_IPV4" | curl --fail --silent --show-error --data-binary @- https://www.duckdns.org/update)
test "$result" = OK || { echo 'DNS update failed' >&2; exit 1; }
echo 'DNS update acknowledged; verify DNS propagation before deployment.'
