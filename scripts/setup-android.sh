#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/android-common.sh"

printf 'Tandara Parent Android setup\n'
printf 'Project: %s\n\n' "$PROJECT_ROOT"
printf 'Checking existing toolchain. This script does not install packages or download Android Studio.\n\n'

if ! "$SCRIPT_DIR/check-android.sh"; then
  cat <<'EOF'
Setup is blocked by missing prerequisites. Install only the missing official
Ubuntu OpenJDK package or Android SDK platform/build-tools package, then rerun
this script. No system changes were made.
EOF
  exit 1
fi

printf '\nVerifying the project Gradle Wrapper:\n'
cd "$PROJECT_ROOT"
if ! ./gradlew --version; then
  printf '\nGradle Wrapper verification failed. No build or device changes were made.\n' >&2
  exit 1
fi

printf '\nSetup check: PASS\n'
