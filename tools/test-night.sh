#!/usr/bin/env bash
# Force night mode on the Portal to check the amber-on-black looks, then put the real window back.
#   tools/test-night.sh on     night all day (the clock picks it up within a second)
#   tools/test-night.sh off    restore the night window that was saved when it was forced on
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/env.sh
PKG=com.acechase.kidclock
case "${1:-}" in
  on)  ON=true ;;
  off) ON=false ;;
  *)   echo "usage: $0 on|off" >&2; exit 2 ;;
esac
adb -s "$PORTAL_SERIAL" shell am broadcast --include-stopped-packages -n $PKG/.alarm.BootReceiver -a $PKG.TEST_NIGHT --ez on $ON
