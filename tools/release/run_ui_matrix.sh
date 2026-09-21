#!/usr/bin/env bash
set -euo pipefail

mkdir -p build/ui-proof
APP_PROOF="/sdcard/Android/data/com.terminator364.kinlink/files/ui-proof"

run_matrix() {
  local name="$1"
  local size="$2"
  local font="$3"

  echo "=== UI MATRIX: $name / size=$size / font=$font ==="
  adb shell wm density 420
  adb shell wm size "$size"
  adb shell settings put system font_scale "$font"
  adb shell am force-stop com.terminator364.kinlink || true

  gradle :app:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.matrixId="$name" \
    --stacktrace

  mkdir -p "build/ui-proof/$name"
  adb pull "$APP_PROOF/$name/." "build/ui-proof/$name/"
}

run_matrix narrow-normal 840x1680 1.0
run_matrix medium-large 945x1890 1.3
run_matrix phone-xlfont 1080x1920 1.6

count="$(find build/ui-proof -type f -name '*.png' | wc -l | tr -d ' ')"
test "$count" -eq 36
echo "Rendered responsive proof PASS: $count screenshots."
