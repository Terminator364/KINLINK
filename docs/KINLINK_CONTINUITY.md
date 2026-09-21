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
   - send the Gmail start-of-tranche message first and apply the Gmail label `KINLINK`;
   - execute one coherent 25-minute tranche, not micro-betas;
   - intermediate user feedback does not interrupt or restart the active tranche;
   - the only permitted early stop is a true human gate objectively required to continue;
   - send the complete end-of-tranche report by Gmail before any ChatGPT-app response and apply label `KINLINK`;
   - only after the end mail is sent, the ChatGPT app response is the short instruction to check Gmail plus Kinshasa date/time;
   - do not duplicate the technical report in the app unless Gmail delivery fails;
   - never attach installation APKs to Gmail; use the KINLINK Drive delivery tree.
   - canonical machine-readable protocol: `.project-memory/COMMUNICATION_PROTOCOL.json`.

## Current durable state

- Canonical Drive rollback/stable installer: `0.6.0-rc3`, versionCode 7.
- Phone currently runs the exact signed `0.7.0`, versionCode 8 field build.
- Installed 0.7.0 is usable but **not eligible for canonical promotion** after post-install counter-audit.
- Active consolidated successor: `0.7.1`, versionCode 9.
- No micro-beta chain: do not request another install until 0.7.1 is fully audited, machine-green, stable-signed and Drive-staged.
- Telemetry schema remains v5.
- Last fully green exact successor head before newest hardening: `a8e4b1a6510ee3bf06d1143253b08437e745cab4`.
- Current counter-audit repairs extend through CA-009.
- Drive delivery folders:
  - INSTALLER = canonical promoted build only;
  - FIELD_CANDIDATE = one exact noncanonical candidate;
  - SIGNING = private signing material only.
- Gmail label for project reports: `KINLINK`.
- Machine-readable tranche protocol: `.project-memory/COMMUNICATION_PROTOCOL.json`.

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

1. Finish exact-head Android CI + design-lint for the full 0.7.1 CA-001..CA-009 batch.
2. Download and read back the exact unsigned 0.7.1 CI artifact.
3. Run second counter-audit on that exact source/artifact identity.
4. Stable-sign exactly that artifact with the canonical KINLINK certificate.
5. Verify signed hash/signer and Drive FIELD_CANDIDATE readback.
6. Only then ask for one human in-place 0.7.1 install.
7. Collect versionCode 9 handoff + resource evidence.
8. Promote to canonical INSTALLER only after Stage B PASS.

## Exact short code to give the user

`KINLINKGO`
