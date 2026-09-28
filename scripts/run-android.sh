#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/android-common.sh"

DEVICE_SERIAL=""
SHOW_LOGS=false
NO_BUILD=false
CHECK_ONLY=false
CLEAN_BUILD=false
API_URL=""

usage() {
  cat <<'EOF'
Usage: ./scripts/run-android.sh [--device <serial>] [--api-url <url>] [--logs] [--no-build] [--clean] [--check]

  --device <serial>  Select one exact ADB device when multiple are connected
  --api-url <url>    Build with the Tandara backend URL, e.g. http://LAN_IP:8000/
  --logs             Follow logs for the launched Tandara Parent process
  --no-build         Install only an existing debug APK
  --clean            Run ./gradlew clean before assembleDebug
  --check            Run the read-only environment check and exit
EOF
}

while (($#)); do
  case "$1" in
    --device)
      if (($# < 2)) || [[ -z "$2" || "$2" == --* ]]; then
        printf 'Missing serial after --device.\n' >&2
        usage >&2
        exit 2
      fi
      DEVICE_SERIAL="$2"
      shift 2
      ;;
    --logs)
      SHOW_LOGS=true
      shift
      ;;
    --api-url)
      if (($# < 2)) || [[ ! "$2" =~ ^https?://[^[:space:]]+/?$ ]]; then
        printf 'Invalid URL after --api-url. Use http://LAN_IP:PORT/ or https://host/.\n' >&2
        exit 2
      fi
      API_URL="$2"
      shift 2
      ;;
    --no-build)
      NO_BUILD=true
      shift
      ;;
    --clean)
      CLEAN_BUILD=true
      shift
      ;;
    --check)
      CHECK_ONLY=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      printf 'Unknown option: %s\n' "$1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [[ "$CHECK_ONLY" == true ]]; then
  if [[ -n "$DEVICE_SERIAL" || -n "$API_URL" || "$SHOW_LOGS" == true || "$NO_BUILD" == true || "$CLEAN_BUILD" == true ]]; then
    printf '%s\n' '--check cannot be combined with --device, --logs, --no-build, or --clean.' >&2
    exit 2
  fi
  exec "$SCRIPT_DIR/check-android.sh"
fi
if [[ "$NO_BUILD" == true && "$CLEAN_BUILD" == true ]]; then
  printf '%s\n' '--clean cannot be combined with --no-build.' >&2
  exit 2
fi

if ! "$SCRIPT_DIR/check-android.sh"; then
  printf '\nAndroid environment check failed. Build and install were not started.\n' >&2
  exit 1
fi
android_detect_environment >/dev/null

rows="$(android_device_rows)"
if [[ -z "$rows" ]]; then
  if [[ -n "$DEVICE_SERIAL" ]]; then
    printf 'Requested device is not connected: %s\n' "$DEVICE_SERIAL" >&2
  fi
  cat <<'EOF'
No Android device is connected.
Connect the phone by USB, enable Developer options and USB debugging, then
approve the computer's USB debugging prompt and rerun this command.
EOF
  exit 1
fi

if [[ -n "$DEVICE_SERIAL" ]]; then
  selected_row=""
  while IFS=$'\t' read -r serial state; do
    if [[ "$serial" == "$DEVICE_SERIAL" ]]; then
      selected_row="$(printf '%s\t%s' "$serial" "$state")"
      break
    fi
  done <<< "$rows"
  if [[ -z "$selected_row" ]]; then
    printf 'Requested device was not found: %s\nConnected devices:\n%s\n' "$DEVICE_SERIAL" "$rows" >&2
    exit 1
  fi
else
  row_count="$(printf '%s\n' "$rows" | wc -l | tr -d ' ')"
  if (( row_count != 1 )); then
    printf 'Multiple ADB devices are connected. Select one explicitly with --device <serial>:\n%s\n' "$rows" >&2
    exit 1
  fi
  selected_row="$rows"
fi

IFS=$'\t' read -r DEVICE_SERIAL DEVICE_STATE <<< "$selected_row"
case "$DEVICE_STATE" in
  device) ;;
  unauthorized)
    printf 'Device %s is unauthorized. Approve the USB debugging prompt on the phone, then retry.\n' "$DEVICE_SERIAL" >&2
    exit 1
    ;;
  offline)
    printf 'Device %s is offline. Reconnect the USB cable or restart USB debugging, then retry.\n' "$DEVICE_SERIAL" >&2
    exit 1
    ;;
  *)
    printf 'Device %s is not ready (ADB state: %s).\n' "$DEVICE_SERIAL" "$DEVICE_STATE" >&2
    exit 1
    ;;
esac

if [[ "$NO_BUILD" == true ]]; then
  if [[ ! -s "$APK_PATH" ]]; then
    printf 'No existing debug APK found at %s. --no-build will not build one.\n' "$APK_PATH" >&2
    exit 1
  fi
else
  if [[ "$CLEAN_BUILD" == true ]]; then
    printf '\nCleaning project build outputs with Gradle Wrapper...\n'
    cd "$PROJECT_ROOT"
    if ! ./gradlew clean; then
      printf '\nBUILD FAILED. No APK was installed.\n' >&2
      exit 1
    fi
  fi
  printf '\nBuilding Tandara Parent debug APK...\n'
  cd "$PROJECT_ROOT"
  gradle_args=(assembleDebug)
  if [[ -n "$API_URL" ]]; then gradle_args+=("-PTANDARA_API_BASE_URL=$API_URL"); fi
  if ! ./gradlew "${gradle_args[@]}"; then
    printf '\nBUILD FAILED. No APK was installed.\n' >&2
    exit 1
  fi
  printf '\nBUILD SUCCESS\n'
fi

if [[ ! -s "$APK_PATH" ]]; then
  printf 'Debug APK is missing or empty: %s\n' "$APK_PATH" >&2
  exit 1
fi
APK_ID="$(android_apk_application_id "$APK_PATH")"
if [[ "$APK_ID" != "$APPLICATION_ID" ]]; then
  printf 'APK identity mismatch: expected %s, found %s. Nothing was installed.\n' "$APPLICATION_ID" "${APK_ID:-unknown}" >&2
  exit 1
fi
launcher_activity="$(android_apk_launcher_activity "$APK_PATH")" || {
  printf 'APK launcher validation failed. Nothing was installed.\n' >&2
  exit 1
}
component="$APPLICATION_ID/$launcher_activity"
printf 'Verified APK application ID: %s\n' "$APK_ID"

printf 'Installing/updating on %s...\n' "$(android_device_model "$DEVICE_SERIAL")"
if ! install_output="$("$ADB_PATH" -s "$DEVICE_SERIAL" install -r "$APK_PATH" 2>&1)"; then
  printf '%s\n' "$install_output" >&2
  if grep -q 'INSTALL_FAILED_UPDATE_INCOMPATIBLE' <<<"$install_output"; then
    printf '\nExisting Tandara Parent installation uses a different signing key.\n' >&2
    printf 'Manual cleanup (removes local app data): adb -s %s uninstall %s\n' "$DEVICE_SERIAL" "$APPLICATION_ID" >&2
  fi
  printf 'INSTALL: FAILED\n' >&2
  exit 1
fi
printf '%s\n' "$install_output"
printf 'INSTALL: PASS\n'

printf 'Launching Tandara Parent...\n'
if ! launch_output="$("$ADB_PATH" -s "$DEVICE_SERIAL" shell am start -W -n "$component" 2>&1)"; then
  printf '%s\n' "$launch_output" >&2
  printf 'APP LAUNCH: FAILED\n' >&2
  exit 1
fi
printf '%s\n' "$launch_output"
app_pid=""
resumed_activity=""
for attempt in 1 2 3 4 5; do
  app_pid="$("$ADB_PATH" -s "$DEVICE_SERIAL" shell pidof "$APPLICATION_ID" 2>/dev/null | tr -d '\r' || true)"
  resumed_activity="$("$ADB_PATH" -s "$DEVICE_SERIAL" shell dumpsys activity activities 2>/dev/null | grep -E 'ResumedActivity:|mResumedActivity|topResumedActivity' | grep -F "$APPLICATION_ID/" | head -n 1 || true)"
  if [[ -n "$app_pid" && -n "$resumed_activity" ]]; then
    break
  fi
done
if [[ -z "$app_pid" || -z "$resumed_activity" ]]; then
  printf 'APP LAUNCH: FAILED (Tandara process and resumed activity were not both observed)\n' >&2
  exit 1
fi
printf 'APP LAUNCH: PASS (pid %s)\n' "$app_pid"
printf 'Immediate crash: NOT OBSERVED at launch; manual screen-flow verification remains required.\n'

installed_packages="$("$ADB_PATH" -s "$DEVICE_SERIAL" shell pm list packages 2>/dev/null | tr -d '\r')"
if grep -Fxq "package:$OLD_APPLICATION_ID" <<<"$installed_packages"; then
  printf 'OLD PACKAGE: PRESENT (%s)\n' "$OLD_APPLICATION_ID"
  printf 'Manual cleanup only (removes old app data): adb -s %s uninstall %s\n' "$DEVICE_SERIAL" "$OLD_APPLICATION_ID"
else
  printf 'OLD PACKAGE: NOT PRESENT\n'
fi
if grep -Fxq "package:$APPLICATION_ID" <<<"$installed_packages"; then
  printf 'NEW PACKAGE: INSTALLED\n'
else
  printf 'NEW PACKAGE: NOT INSTALLED\n' >&2
  exit 1
fi

if [[ "$SHOW_LOGS" == true ]]; then
  printf '\nShowing logs for Tandara Parent (Ctrl+C to stop)...\n'
  "$ADB_PATH" -s "$DEVICE_SERIAL" logcat -v time | grep --line-buffered -E "$APPLICATION_ID|AndroidRuntime|FATAL EXCEPTION"
fi
