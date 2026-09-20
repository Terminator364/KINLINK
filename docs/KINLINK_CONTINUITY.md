# KINLINK continuity

## Canonical recovery command

`KINLINKGO`

When this command is used in another KINLINK conversation/workspace:

1. Open repository `Terminator364/KINLINK`, branch `main`.
2. Reload, in this order:
   - `project_state.json`
   - `NEXT_UNCOMMITTED_ACTION.md`
   - `BUILD_STATUS.md`
   - `docs/CANONICAL_SPEC.md`
   - this file: `docs/KINLINK_CONTINUITY.md`
3. Read the latest durable Git head and recent CI status.
4. Resume from `next_uncommitted_action`; never restart the project from M0/M1.
5. Keep the installed field baseline immutable unless a new consolidated signed release passes all gates.
6. Never promote a DEV APK over the canonical field APK.
7. Preserve fail-open/mobile-observation-only invariants and CI fences.
8. Prefer large coherent batches over micro-RCs.
9. At tranche end, send the full report by Gmail first. The ChatGPT app response may be only the short instruction to check Gmail.

## Current durable state

- Field baseline: `0.6.0-rc3`, versionCode 7.
- Field handoff gate: Wi-Fi -> cellular -> Wi-Fi PASS.
- Original mobile-block symptom: NOT REPRODUCED.
- Canonical Drive APK remains RC3.
- Development line: `0.7.0-dev`, versionCode 8.
- Promotion of DEV: FORBIDDEN until consolidated RC gate.
- Telemetry schema: v5.
- Latest full machine PASS code head: `00e4df0d3869daaab777d2b159a1c8228d9b2c7c`.
- Current durable docs checkpoint before this file: `cc6dd300ea865637b876233ecd23dacb8181b769`.

## 0.7.0-dev integrated scope

Includes, among other items:
- handoff fail-open hardening;
- passive bandwidth/quality;
- recovery-effectiveness receipts and circuit breakers;
- interruption and low-quality duration histories;
- passive cause diagnosis;
- privacy-safe IPv4/IPv6 topology;
- bounded DNS-vs-HTTP manual Wi-Fi diagnosis;
- radio-quality / congestion / suspended-network diagnosis;
- 24 h reliability summary and trend;
- post-update runtime self-test;
- resource evidence hooks;
- bounded telemetry retention.

## Next durable action

Continue the large 0.7.0-dev batch with:
1. longitudinal radio/congestion evidence;
2. RAM/battery/wakeup qualification;
3. stronger migration proof;
4. consolidated release-readiness gate;
5. no new signed install until the batch is materially larger and all machine gates pass.

## Exact short code to give the user

`KINLINKGO`
