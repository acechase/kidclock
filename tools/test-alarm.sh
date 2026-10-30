#!/usr/bin/env bash
# Exercise the alarm path on the Portal without touching saved alarms.
#   tools/test-alarm.sh [seconds]        ring a test alarm in N seconds (default 10)
#   tools/test-alarm.sh --missed [min]   pretend an alarm N minutes ago (default 5) was lost while
#                                        the device was off; it should ring immediately
#   tools/test-alarm.sh --stop           stop a ringing test alarm and return to the clock
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/env.sh
PKG=com.acechase.kidclock
RECV="am broadcast --include-stopped-packages -n $PKG/.alarm.BootReceiver"
case "${1:-}" in
  --missed) adb -s "$PORTAL_SERIAL" shell $RECV -a $PKG.TEST_MISSED --ei minutes_ago "${2:-5}" ;;
  --stop)   adb -s "$PORTAL_SERIAL" shell am force-stop $PKG
            adb -s "$PORTAL_SERIAL" shell am start -n $PKG/.ClockActivity ;;
  *)        adb -s "$PORTAL_SERIAL" shell $RECV -a $PKG.TEST_ALARM --ei seconds "${1:-10}" ;;
esac
