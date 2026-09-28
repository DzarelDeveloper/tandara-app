#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
APPLICATION_ID="id.tandara.parent"
OLD_APPLICATION_ID="com.aistudio.tandara.kztw"

android_detect_platform() {
  local build_file="$PROJECT_ROOT/app/build.gradle.kts"
  local spec major minor

  spec="$(grep -Eo 'release\([0-9]+\)[^}]*' "$build_file" | head -n 1 || true)"
  if [[ -n "$spec" ]]; then
    major="$(sed -E 's/.*release\(([0-9]+)\).*/\1/' <<<"$spec")"
    minor="$(sed -nE 's/.*minorApiLevel[[:space:]]*=[[:space:]]*([0-9]+).*/\1/p' <<<"$spec")"
    if [[ -n "$minor" ]]; then
      ANDROID_PLATFORM="android-$major.$minor"
    else
      ANDROID_PLATFORM="android-$major"
    fi
    return
  fi

  major="$(grep -Eo 'compileSdk[[:space:]]*=[[:space:]]*[0-9]+' "$build_file" | head -n 1 | grep -Eo '[0-9]+$' || true)"
  if [[ -z "$major" ]]; then
    printf 'Could not determine compileSdk from app/build.gradle.kts.\n' >&2
    return 1
  fi
  ANDROID_PLATFORM="android-$major"
}

android_detect_environment() {
  local candidate java_path java_major sdk_from_properties sdk_candidate build_tools_path
  local -a java_candidates=() sdk_candidates=() build_tools=()

  android_detect_platform

  if [[ -n "${JAVA_HOME:-}" ]]; then
    java_candidates+=("$JAVA_HOME")
  fi
  if command -v javac >/dev/null 2>&1; then
    java_path="$(readlink -f "$(command -v javac)")"
    java_candidates+=("$(dirname -- "$(dirname -- "$java_path")")")
  fi
  shopt -s nullglob
  java_candidates+=("$HOME"/.local/share/tandara-toolchains/jdk*/usr/lib/jvm/*)
  java_candidates+=(/usr/lib/jvm/* /opt/android-studio/jbr /snap/android-studio/current/android-studio/jbr)
  shopt -u nullglob

  JAVA_HOME=""
  for candidate in "${java_candidates[@]}"; do
    if [[ -x "$candidate/bin/java" && -x "$candidate/bin/javac" ]]; then
      java_major="$("$candidate/bin/java" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\1/p')"
      if [[ "$java_major" =~ ^[0-9]+$ ]] && (( java_major >= 17 && java_major <= 25 )); then
        JAVA_HOME="$candidate"
        JAVA_VERSION="$java_major"
        break
      fi
    fi
  done
  if [[ -z "$JAVA_HOME" ]]; then
    printf 'No compatible JDK found. Gradle 9.3.1 requires a supported JDK (17-25); JDK 17 is verified for this project.\n' >&2
    return 1
  fi
  export JAVA_HOME
  PATH="$JAVA_HOME/bin:$PATH"
  export PATH

  sdk_from_properties=""
  if [[ -f "$PROJECT_ROOT/local.properties" ]]; then
    sdk_from_properties="$(sed -nE 's/^[[:space:]]*sdk\.dir[[:space:]]*=[[:space:]]*//p' "$PROJECT_ROOT/local.properties" | head -n 1)"
    sdk_from_properties="${sdk_from_properties//\\:/\:}"
    sdk_from_properties="${sdk_from_properties//\\=/\=}"
    sdk_from_properties="${sdk_from_properties//\\ / }"
  fi
  [[ -n "${ANDROID_HOME:-}" ]] && sdk_candidates+=("$ANDROID_HOME")
  [[ -n "${ANDROID_SDK_ROOT:-}" && "${ANDROID_SDK_ROOT:-}" != "${ANDROID_HOME:-}" ]] && sdk_candidates+=("$ANDROID_SDK_ROOT")
  [[ -n "$sdk_from_properties" ]] && sdk_candidates+=("$sdk_from_properties")
  sdk_candidates+=("$HOME/Android/Sdk" /opt/android-sdk /usr/lib/android-sdk)

  ANDROID_HOME=""
  for sdk_candidate in "${sdk_candidates[@]}"; do
    if [[ -d "$sdk_candidate" ]]; then
      ANDROID_HOME="$(cd -- "$sdk_candidate" && pwd)"
      break
    fi
  done
  if [[ -z "$ANDROID_HOME" ]]; then
    printf 'Android SDK not found. Set ANDROID_HOME/ANDROID_SDK_ROOT or sdk.dir in local.properties.\n' >&2
    return 1
  fi
  ANDROID_SDK_ROOT="$ANDROID_HOME"
  export ANDROID_HOME ANDROID_SDK_ROOT

  ADB_PATH="$ANDROID_HOME/platform-tools/adb"
  if [[ ! -x "$ADB_PATH" ]]; then
    ADB_PATH="$(command -v adb || true)"
  fi
  if [[ -z "$ADB_PATH" || ! -x "$ADB_PATH" ]]; then
    printf 'ADB not found in Android SDK platform-tools or PATH.\n' >&2
    return 1
  fi

  if [[ ! -f "$ANDROID_HOME/platforms/$ANDROID_PLATFORM/android.jar" ]]; then
    printf 'Required SDK platform is missing: %s/platforms/%s\n' "$ANDROID_HOME" "$ANDROID_PLATFORM" >&2
    return 1
  fi

  shopt -s nullglob
  build_tools=("$ANDROID_HOME"/build-tools/*)
  shopt -u nullglob
  BUILD_TOOLS_PATH=""
  if ((${#build_tools[@]})); then
    build_tools_path="$(printf '%s\n' "${build_tools[@]}" | sort -V | tail -n 1)"
    if [[ -x "$build_tools_path/aapt2" || -x "$build_tools_path/aapt" ]]; then
      BUILD_TOOLS_PATH="$build_tools_path"
    fi
  fi
  if [[ -z "$BUILD_TOOLS_PATH" ]]; then
    printf 'Android build-tools are missing or incomplete under %s/build-tools.\n' "$ANDROID_HOME" >&2
    return 1
  fi

  WRAPPER_FILES=(
    "$PROJECT_ROOT/gradlew"
    "$PROJECT_ROOT/gradlew.bat"
    "$PROJECT_ROOT/gradle/wrapper/gradle-wrapper.jar"
    "$PROJECT_ROOT/gradle/wrapper/gradle-wrapper.properties"
  )
  local wrapper_file
  for wrapper_file in "${WRAPPER_FILES[@]}"; do
    if [[ ! -s "$wrapper_file" ]]; then
      printf 'Gradle Wrapper file is missing or empty: %s\n' "$wrapper_file" >&2
      return 1
    fi
  done
  if [[ ! -x "$PROJECT_ROOT/gradlew" ]]; then
    printf 'Gradle Wrapper launcher is not executable: %s/gradlew\n' "$PROJECT_ROOT" >&2
    return 1
  fi
  if ! "$JAVA_HOME/bin/jar" tf "$PROJECT_ROOT/gradle/wrapper/gradle-wrapper.jar" >/dev/null 2>&1; then
    printf 'Gradle Wrapper JAR is invalid.\n' >&2
    return 1
  fi

  APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
  APK_ANALYZER="$ANDROID_HOME/cmdline-tools/latest/bin/apkanalyzer"
  if [[ ! -x "$APK_ANALYZER" ]]; then
    APK_ANALYZER=""
  fi
  export ADB_PATH APK_PATH APK_ANALYZER BUILD_TOOLS_PATH ANDROID_PLATFORM JAVA_VERSION
}

android_device_rows() {
  "$ADB_PATH" devices -l | awk 'NR > 1 && NF >= 2 { print $1 "\t" $2 }'
}

android_device_model() {
  local serial="$1" model
  model="$("$ADB_PATH" -s "$serial" shell getprop ro.product.model 2>/dev/null | tr -d '\r' || true)"
  if [[ -n "$model" ]]; then
    printf '%s' "$model"
  else
    printf '%s' "$serial"
  fi
}

android_apk_application_id() {
  local apk="$1"
  if [[ -n "$APK_ANALYZER" ]]; then
    "$APK_ANALYZER" manifest application-id "$apk"
    return
  fi
  local aapt="$BUILD_TOOLS_PATH/aapt"
  if [[ -x "$aapt" ]]; then
    "$aapt" dump badging "$apk" | sed -nE "s/^package: name='([^']+)'.*/\1/p" | head -n 1
    return
  fi
  printf 'No APK analyzer (apkanalyzer or aapt) is available.\n' >&2
  return 1
}

android_apk_launcher_activity() {
  local apk="$1" aapt launcher
  aapt="$BUILD_TOOLS_PATH/aapt"
  if [[ ! -x "$aapt" ]]; then
    printf 'aapt is unavailable; cannot determine the APK launcher activity.\n' >&2
    return 1
  fi
  launcher="$("$aapt" dump badging "$apk" | sed -nE "s/^launchable-activity: name='([^']+)'.*/\\1/p" | head -n 1)"
  if [[ -z "$launcher" ]]; then
    printf 'No launcher activity found in APK metadata.\n' >&2
    return 1
  fi
  if [[ "$launcher" == .* ]]; then
    launcher="$APPLICATION_ID$launcher"
  fi
  printf '%s' "$launcher"
}
