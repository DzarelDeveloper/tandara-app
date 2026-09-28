#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/android-common.sh"

printf 'Tandara Parent - Android Environment\n\n'
if ! android_detect_environment; then
  printf '\nSTATUS: BLOCKED\n' >&2
  exit 1
fi

printf '%-24s %s\n' 'Java' "OK (JDK $JAVA_VERSION)"
printf '%-24s %s\n' 'JAVA_HOME' "$JAVA_HOME"
printf '%-24s %s\n' 'Android SDK' "$ANDROID_HOME"
printf '%-24s %s\n' 'ADB' "$ADB_PATH"
printf '%-24s %s\n' 'Gradle Wrapper' 'OK (all files present; JAR valid)'
printf '%-24s %s\n' 'Android Platform' "$ANDROID_PLATFORM"
printf '%-24s %s\n' 'Build Tools' "${BUILD_TOOLS_PATH##*/}"
printf '\nApplication ID: %s\n' "$APPLICATION_ID"
printf 'USB Devices:\n'

if ! "$ADB_PATH" version >/dev/null 2>&1; then
  printf '  ADB could not start.\n'
  printf '\nSTATUS: BLOCKED\n' >&2
  exit 1
fi

rows="$(android_device_rows)"
if [[ -z "$rows" ]]; then
  printf '  None connected\n'
  printf 'Authorization: NOT AVAILABLE (connect and authorize a phone to run)\n'
else
  while IFS=$'\t' read -r serial state; do
    [[ -n "$serial" ]] || continue
    case "$state" in
      device)
        printf '  %s (%s) - AUTHORIZED\n' "$(android_device_model "$serial")" "$serial"
        ;;
      unauthorized)
        printf '  %s - UNAUTHORIZED (approve the USB debugging prompt)\n' "$serial"
        ;;
      offline)
        printf '  %s - OFFLINE (reconnect the cable or restart ADB on the phone)\n' "$serial"
        ;;
      *)
        printf '  %s - %s\n' "$serial" "$state"
        ;;
    esac
  done <<< "$rows"
fi

printf '\nSTATUS: READY (environment checks passed)\n'
