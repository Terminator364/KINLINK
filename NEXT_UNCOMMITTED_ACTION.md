# NEXT_UNCOMMITTED_ACTION

## Stable field baseline
0.6.0-rc3 remains installed and Drive-canonical. Do not replace it with a DEV build.

## Active development line
0.7.0-dev / versionCode 8.

## Latest proven full machine PASS
`858e63b92543df4dbed924dc9152a1161e63f168`.

## Current code head under qualification
`1b404fa59aba02746f68291f21f8b8a5e533544f`.

Design-lint: PASS.
Android CI: IN_PROGRESS at checkpoint time.

## Newly integrated
1. longitudinal passive radio/congestion/suspension evidence;
2. explicit contiguous telemetry migration plan v1 -> v5, now used by TelemetryLedger.onUpgrade();
3. conservative runtime resource qualification gate;
4. automatic >=30-minute resource verdict using PSS, battery-rate evidence and callback churn;
5. longitudinal radio evidence surfaced in diagnostic export.

## Next durable action
1. read final Android CI result for `1b404fa59aba02746f68291f21f8b8a5e533544f`;
2. if PASS, promote it to latest full machine PASS in canonical state;
3. continue consolidated release-readiness qualification;
4. target-device >=30-minute evidence remains required before signed promotion;
5. no new signed install until the consolidated gate passes.
