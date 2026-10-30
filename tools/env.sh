#!/usr/bin/env bash
# Source this: `. tools/env.sh`. Uses JAVA_HOME/ANDROID_HOME if set; otherwise prefers Homebrew JDK 17
# and falls back to the JDK and SDK that ship with Unity (this machine happens to have Unity installed).
UNITY_PLAYER=/Applications/Unity/Hub/Editor/6000.3.22f1/PlaybackEngines/AndroidPlayer
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  : # respect a JDK 17 the caller already chose
elif [ -d /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ]; then
  export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
elif [ -d "$UNITY_PLAYER/OpenJDK" ]; then
  export JAVA_HOME="$UNITY_PLAYER/OpenJDK"
fi
export ANDROID_HOME="${ANDROID_HOME:-$UNITY_PLAYER/SDK}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
# The Portal's adb serial. Defaults to the first (or only) device adb can see; set PORTAL_SERIAL to pin one.
export PORTAL_SERIAL="${PORTAL_SERIAL:-$(adb devices 2>/dev/null | awk 'NR>1 && $2=="device" {print $1; exit}')}"
