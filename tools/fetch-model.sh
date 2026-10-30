#!/usr/bin/env bash
# Downloads the offline Vosk speech model into app assets (it is not committed; ~68 MB).
set -euo pipefail
cd "$(dirname "$0")/.."
DEST=app/src/main/assets/vosk-model
[ -f "$DEST/am/final.mdl" ] && { echo "model already present"; exit 0; }
TMP=$(mktemp -d)
curl -sSL -o "$TMP/m.zip" https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
unzip -q "$TMP/m.zip" -d "$TMP"
rm -rf "$DEST"; mkdir -p "$(dirname "$DEST")"
mv "$TMP/vosk-model-small-en-us-0.15" "$DEST"
rm -f "$DEST/README"; echo 1 > "$DEST/version"
rm -rf "$TMP"; echo "model installed to $DEST"
