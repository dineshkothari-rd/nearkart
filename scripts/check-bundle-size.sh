#!/bin/sh
set -eu

assets=${1:-frontend/dist/assets}
[ -d "$assets" ] || { echo "Build assets not found: $assets" >&2; exit 1; }

js_bytes=$(find "$assets" -type f -name '*.js' -exec wc -c {} + | awk 'END { print $1 + 0 }')
css_bytes=$(find "$assets" -type f -name '*.css' -exec wc -c {} + | awk 'END { print $1 + 0 }')
[ "$js_bytes" -le 500000 ] || { echo "JavaScript budget exceeded: $js_bytes > 500000 bytes" >&2; exit 1; }
[ "$css_bytes" -le 100000 ] || { echo "CSS budget exceeded: $css_bytes > 100000 bytes" >&2; exit 1; }
echo "Bundle budget passed: JS $js_bytes/500000 bytes, CSS $css_bytes/100000 bytes."
