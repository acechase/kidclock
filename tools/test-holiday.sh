#!/usr/bin/env bash
# Show a holiday design on the Portal now, to check it without waiting for the date.
#   tools/test-holiday.sh halloween      pretend today is in Halloween's window
#   tools/test-holiday.sh off            back to the real calendar
# Ids: newyear valentine stpatrick easter july4 halloween thanksgiving christmas
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/env.sh
PKG=com.acechase.kidclock
ID="${1:-}"
case "$ID" in
  newyear|valentine|stpatrick|easter|july4|halloween|thanksgiving|christmas|off) ;;
  *) echo "usage: $0 newyear|valentine|stpatrick|easter|july4|halloween|thanksgiving|christmas|off" >&2; exit 2 ;;
esac
adb -s "$PORTAL_SERIAL" shell am broadcast --include-stopped-packages -n $PKG/.alarm.BootReceiver -a $PKG.TEST_HOLIDAY --es id "$ID"
