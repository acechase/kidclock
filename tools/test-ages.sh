#!/usr/bin/env bash
# Set the kids' ages from adb, e.g. tools/test-ages.sh 6 9. The early-reader screens (short line on the quote,
# weather tiles, picture buttons) follow the youngest age; any per-screen overrides are cleared.
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/env.sh
PKG=com.acechase.kidclock
adb -s "$PORTAL_SERIAL" shell am broadcast --include-stopped-packages -n $PKG/.alarm.BootReceiver -a $PKG.TEST_AGES --ei youngest "${1:-11}" --ei oldest "${2:-${1:-11}}"
