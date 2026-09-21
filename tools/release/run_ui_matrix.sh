#!/usr/bin/env bash
set -euo pipefail

mkdir -p build/ui-proof
APP_PROOF="/data/local/tmp/KINLINK-ui-proof"

archive_test_evidence() {
  local name="$1"
  local dest="build/ui-proof/$name"
  mkdir -p "$dest"

  adb pull "$APP_PROOF/$name/." "$dest/" >/dev/null 2>&1 || true

  if [ -d app/build/reports/androidTests/connected/debug ]; then
    rm -rf "$dest/test-report"
    cp -R app/build/reports/androidTests/connected/debug "$dest/test-report"
  fi

  if [ -d app/build/outputs/androidTest-results/connected/debug ]; then
    rm -rf "$dest/test-results"
    cp -R app/build/outputs/androidTest-results/connected/debug "$dest/test-results"
  fi
}

print_failure_evidence() {
  echo "=== INSTRUMENTATION FAILURE EVIDENCE ==="
  grep -R -n -E 'AssertionError|NoSuch(Field|Method)|Exception|failure|failed|clipped|ellipsized|overlap|tap target|collapsed|Protection'     app/build/outputs/androidTest-results     app/build/reports/androidTests 2>/dev/null | tail -200 || true
  echo "=== END FAILURE EVIDENCE ==="
}

run_matrix() {
  local name="$1"
  local size="$2"
  local font="$3"

  echo "=== UI MATRIX: $name / size=$size / font=$font ==="
  adb shell wm density 420
  adb shell wm size "$size"
  adb shell settings put system font_scale "$font"
  adb shell rm -rf "$APP_PROOF/$name" || true
  adb shell am force-stop com.google.android.apps.nexuslauncher || true
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
  adb shell am force-stop com.terminator364.kinlink || true

  set +e
  gradle :app:connectedDebugAndroidTest     -Pandroid.testInstrumentationRunnerArguments.matrixId="$name"     --stacktrace --info
  local rc=$?
  set -e

  archive_test_evidence "$name"

  if [ "$rc" -ne 0 ]; then
    print_failure_evidence
    return "$rc"
  fi
}

run_matrix narrow-normal 840x1680 1.0
run_matrix medium-large 945x1890 1.3
run_matrix phone-xlfont 1080x1920 1.6

count="$(find build/ui-proof -type f -name '*.png' | wc -l | tr -d ' ')"
test "$count" -eq 42
echo "Rendered responsive proof PASS: $count screenshots (including data dialog + technical details)."
