#!/bin/sh
set -eu

WAR_PATH="${1:?Usage: patch-war-datasource.sh <war-path> <datasource-url> [h2-web-allow-others] }"
DATASOURCE_URL="${2:?Usage: patch-war-datasource.sh <war-path> <datasource-url> [h2-web-allow-others] }"
H2_WEB_ALLOW_OTHERS="${3:-false}"
PROPERTIES_PATH="WEB-INF/classes/application.properties"

if [ ! -f "$WAR_PATH" ]; then
  echo "WAR not found: $WAR_PATH" >&2
  exit 1
fi

case "$WAR_PATH" in
  /*) ;;
  *) WAR_PATH="$(pwd)/$WAR_PATH" ;;
esac

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

(
  cd "$WORK_DIR"
  jar xf "$WAR_PATH" "$PROPERTIES_PATH"
)

if [ ! -f "$WORK_DIR/$PROPERTIES_PATH" ]; then
  echo "$PROPERTIES_PATH not found in $WAR_PATH" >&2
  exit 1
fi

set_property() {
  KEY="$1"
  VALUE="$2"
  ESCAPED_VALUE="$(printf '%s' "$VALUE" | sed 's/[&|\\]/\\&/g')"

  if grep -q "^$KEY=" "$WORK_DIR/$PROPERTIES_PATH"; then
    sed -i "s|^$KEY=.*|$KEY=$ESCAPED_VALUE|" "$WORK_DIR/$PROPERTIES_PATH"
  else
    printf '\n%s=%s\n' "$KEY" "$VALUE" >> "$WORK_DIR/$PROPERTIES_PATH"
  fi
}

set_property "spring.datasource.url" "$DATASOURCE_URL"
set_property "spring.h2.console.settings.web-allow-others" "$H2_WEB_ALLOW_OTHERS"

jar uf "$WAR_PATH" -C "$WORK_DIR" "$PROPERTIES_PATH"

echo "Patched $PROPERTIES_PATH:"
grep '^spring.datasource.url=' "$WORK_DIR/$PROPERTIES_PATH"
grep '^spring.h2.console.settings.web-allow-others=' "$WORK_DIR/$PROPERTIES_PATH"
