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
9. Communication protocol is mandatory for every substantial KINLINKGO tranche (current cadence: 25 minutes):
   - send a Gmail start-of-tranche message first;
   - execute one coherent 25-minute tranche, not micro-betas;
   - send the complete end-of-tranche report by Gmail first;
   - in the ChatGPT app, respond only with a short instruction to check Gmail plus Kinshasa date/time;
   - do not duplicate the technical report in the app unless Gmail delivery fails.

## Current durable state

- Field baseline: `0.6.0-rc3`, versionCode 7.
- Field handoff gate: Wi-Fi -> cellular -> Wi-Fi PASS.
- Original mobile-block symptom: NOT REPRODUCED.
- Canonical Drive APK remains RC3.
- Development line: `0.7.0-dev`, versionCode 8.
- Promotion of DEV: FORBIDDEN until consolidated RC gate.
- Telemetry schema: v5.
- Latest full machine PASS code head: `4153720cde8c6bd0004b8e69baeadf5254735abe`.
- Final consolidated field-candidate code head: `4153720cde8c6bd0004b8e69baeadf5254735abe`.

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

1. deliver/install only the exact final signed 0.7.0 APK SHA-256 `cea3468340a8f81dc38cc1ba09abb68e8f9ccc1634e223f275b0e92b083ef0bc` over RC3;
2. never uninstall RC3 merely to force the update;
3. collect versionCode 8 FIELD_HANDOFF + >=30-minute RESOURCE_QUALIFICATION evidence automatically;
4. old RC3 evidence cannot qualify v8;
5. canonical RC3 remains Drive-canonical until Stage B passes.

## Exact short code to give the user

`KINLINKGO`
