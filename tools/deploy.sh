#!/usr/bin/env bash
# Build, install, grant the mic permission, and (optionally) make KidClock the Portal's HOME.
#   tools/deploy.sh            build + install + grant
#   tools/deploy.sh --set-home also run unmetaportal's kiosk script with --set-home
set -euo pipefail
cd "$(dirname "$0")/.."
. tools/env.sh
PKG=com.acechase.kidclock
SET_HOME=0
for a in "$@"; do [[ "$a" == "--set-home" ]] && SET_HOME=1; done

./gradlew -q :app:assembleDebug
APK=app/build/outputs/apk/debug/app-debug.apk
adb -s "$PORTAL_SERIAL" install -r -g "$APK"
adb -s "$PORTAL_SERIAL" shell pm grant $PKG android.permission.RECORD_AUDIO || true
adb -s "$PORTAL_SERIAL" shell appops set $PKG RECORD_AUDIO allow || true
echo "installed $PKG"

if [[ $SET_HOME -eq 1 ]]; then
  UNMETA="${UNMETA_DIR:-$HOME/unmetaportal}"
  "$UNMETA/tools/portal-kiosk-enable.sh" --component "$PKG/.ClockActivity" --set-home --yes
else
  adb -s "$PORTAL_SERIAL" shell am start -n "$PKG/.ClockActivity"
fi
