#!/usr/bin/env bash
# Switch the Portal's background and look from adb (for screenshots), e.g. tools/test-design.sh 8 9
#   face: 0 Sky 1 Ocean 2 Rainbow 3 Galaxy 4 Fireflies 5 Aurora 6 Meadow 7 Doodle Strings 8 Notebook
#   look: 0 Classic 1 Bold 2 Stacked 3 Analog 4 Sketch 5 Whacky 6 Pixel 7 Magnets 8 Tiara 9 Marker 10 Marker Stacked
# Pass -1 to leave one as it is. Showing a design this way dismisses any holiday design for the day.
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/env.sh
PKG=com.acechase.kidclock
adb -s "$PORTAL_SERIAL" shell am broadcast --include-stopped-packages -n $PKG/.alarm.BootReceiver -a $PKG.TEST_DESIGN --ei face "${1:--1}" --ei look "${2:--1}"
