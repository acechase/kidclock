#!/usr/bin/env bash
# Open the weather page on the Portal dressed for a sky (for screenshots), e.g. tools/test-weather.sh RAINY
#   skies: SUNNY CLOUDY RAINY SNOWY STORMY NIGHT; no argument = whatever the real weather picks.
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/env.sh
PKG=com.acechase.kidclock
adb -s "$PORTAL_SERIAL" shell am broadcast --include-stopped-packages -n $PKG/.alarm.BootReceiver -a $PKG.TEST_WEATHER ${1:+--es sky "$1"}
