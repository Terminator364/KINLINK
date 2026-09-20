# NEXT_UNCOMMITTED_ACTION

## Stable field baseline
0.6.0-rc3 remains installed and Drive-canonical.

## Active development line
0.7.0-dev / versionCode 8.

## This tranche integrated
1. passive Wi-Fi radio signal model;
2. Android congestion/suspension evidence;
3. weak-radio / possible-congestion / suspended-network causes;
4. cause-aware Autopilot suppression of useless recovery;
5. visible 24 h reliability summary;
6. recent passive quality trend;
7. profile recommendation from reliability burden;
8. slow-link burden contributes to reliability severity.

## Current gate
Complete design-lint + Android CI for code head `00e4df0d3869daaab777d2b159a1c8228d9b2c7c`.

## After gate
Continue large 0.7.0-dev functionality:
- longitudinal radio/cause evidence;
- resource and wakeup qualification;
- migration proof;
- consolidated release-readiness gate;
- no new signed install until the feature batch is materially larger and all gates pass.
