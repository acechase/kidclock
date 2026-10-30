#!/usr/bin/env bash
# Flip the "Always night" setting on the Portal from adb, to check the amber-on-black looks.
#   tools/test-night.sh on     night all day (the clock picks it up within a second)
#   tools/test-night.sh off    back to the normal night window
# The same switch is in Settings, so this only saves a trip through the corner-hold menu.
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
