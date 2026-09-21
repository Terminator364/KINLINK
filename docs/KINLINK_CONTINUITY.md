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
- Latest full machine PASS code head: `71111619ada624bdf717e869323ac309bd07c7de`.
- Current functional code head under Android CI qualification: `e1e2e7169d6a13f5fce8b808574cc63ed9fe915d`.

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
1. finish Android CI qualification of `e1e2e7169d6a13f5fce8b808574cc63ed9fe915d`;
2. close Stage A: MACHINE + MIGRATION + SIGNER_CONTINUITY;
3. only then produce one consolidated signed field candidate, without replacing RC3;
4. collect FIELD_HANDOFF + >=30-minute RESOURCE_QUALIFICATION on that candidate;
5. promote canonically only when all five gates are PASS; no micro-beta chain.

## Exact short code to give the user

`KINLINKGO`
