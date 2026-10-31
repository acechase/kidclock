#!/usr/bin/env bash
# Download the bundled typefaces (all SIL Open Font License) from Google Fonts into app/src/main/res/font.
# Re-run after adding a line to FONTS below; existing files are replaced. See FONTS.md for the licence table.
#   tools/fetch-fonts.sh            fetch every font that is missing
#   tools/fetch-fonts.sh --all      fetch them all again
set -euo pipefail
cd "$(dirname "$0")/.."
DEST=app/src/main/res/font
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

# resource name | css2 family spec (as in a fonts.googleapis.com/css2?family= URL)
FONTS="
fredoka_semibold|Fredoka:wght@600
oswald_medium|Oswald:wght@500
caveat_bold|Caveat:wght@700
patrick_hand|Patrick+Hand
bungee_shade|Bungee+Shade
monoton|Monoton
rubik_mono_one|Rubik+Mono+One
press_start_2p|Press+Start+2P
cormorant_garamond_semibold|Cormorant+Garamond:wght@600
great_vibes|Great+Vibes
cinzel_semibold|Cinzel:wght@600
josefin_sans_light|Josefin+Sans:wght@300
playfair_display_italic_medium|Playfair+Display:ital,wght@1,500
parisienne|Parisienne
uncial_antiqua|Uncial+Antiqua
comfortaa_bold|Comfortaa:wght@700
quicksand_semibold|Quicksand:wght@600
bebas_neue|Bebas+Neue
fraunces_semibold|Fraunces:wght@600
lora_italic_medium|Lora:ital,wght@1,500
libre_baskerville_bold|Libre+Baskerville:wght@700
pinyon_script|Pinyon+Script
"

ALL=0; [[ "${1:-}" == "--all" ]] && ALL=1
while IFS='|' read -r name spec; do
  [[ -z "$name" ]] && continue
  out="$DEST/$name.ttf"
  if [[ $ALL -eq 0 && -s "$out" ]]; then continue; fi
  # Without a browser user agent the css2 endpoint answers with plain TrueType files.
  url=$(curl -fsS "https://fonts.googleapis.com/css2?family=$spec&display=swap" | grep -o 'url([^)]*\.ttf)' | head -1 | sed 's/url(//; s/)//')
  if [[ -z "$url" ]]; then echo "no TTF url for $spec" >&2; exit 1; fi
  curl -fsS -o "$TMP/$name.ttf" "$url"
  # Sanity check: a TrueType file starts with 0x00010000 (or 'true').
  magic=$(head -c 4 "$TMP/$name.ttf" | xxd -p)
  if [[ "$magic" != "00010000" && "$magic" != "74727565" ]]; then echo "$name: not a TrueType file ($magic)" >&2; exit 1; fi
  mv "$TMP/$name.ttf" "$out"
  printf '%-32s %7d bytes  %s\n' "$name" "$(stat -f%z "$out")" "$url"
done <<< "$FONTS"
echo "fonts in $DEST: $(ls "$DEST" | wc -l | tr -d ' ')"
