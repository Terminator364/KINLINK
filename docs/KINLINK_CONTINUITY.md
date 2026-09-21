# KINLINK continuity v3

## Canonical recovery command
`KINLINKGO`

## Mandatory first reads
Load project state, next action, build status, canonical spec, this file, takeover state, communication protocol/state machine, active tranche, delivery ledger, field-candidate freeze, and `.project-memory/DELIVERY_SECURITY_POLICY.json`.

Never reconstruct state from chat when durable state exists.

## Communication safety
- 25 minutes = 22 minutes work + 3 minutes closeout.
- Gmail START provider ACK before substantive work.
- Every new work period opens a new durable tranche; CLOSED tranches are never reused.
- Closeout: checkpoint -> CLOSE_INTENT -> Gmail END -> provider ACK -> durable END_ACK/CLOSED -> short app pointer.
- No final ChatGPT closeout while END is unacknowledged.
- APKs are never sent by Gmail.

## Security continuity
- Never bypass platform/security controls.
- On a platform/security hold: checkpoint, stop blocked/risky operations, preserve idempotency, and resume from the last proof after the control clears.
- Private signing material remains confined to the private signing area and never enters public GitHub, Gmail, or human delivery folders.

## Human delivery contract
The only normal installation path is:
`KINLINK/INSTALLER/KINLINK_INSTALL_NOW.apk`

Folder ID: `1HjnK_CtA_Bg5d19twTnYvGghCKY3AIc8`.
It must contain exactly one APK.

Current authorized candidate:
- 0.7.3-dev / versionCode 11
- source `2aeac4cf801e60986c33347676e12310790d1d98`
- Android CI `35626585156`: PASS
- design-lint `35626585191`: PASS
- 36 rendered UI captures: PASS assertions + visual review
- SHA-256 `d268542a4e6d4a4a5869deddd71bf0aec32c46386d2193cf96da6b825e68877d`
- signer `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Drive file ID `1dj2j2zhLmUPb63NVIaMx0jElpNdb04ky`
- Drive readback exact-match PASS

Installed target: 0.7.2 / versionCode 10.
Compatibility: package PASS; 11 > 10 PASS; signer continuity PASS.

## Rollback isolation
Former INSTALLER is now `KINLINK/ROLLBACK_CANONICAL_DO_NOT_INSTALL`.
It contains 0.6.0-rc3/versionCode7 and is not a normal update source.

## Current next gate
One in-place install of `KINLINK_INSTALL_NOW.apk` over 0.7.2. Do not uninstall first. Then verify v11 startup/UI and field telemetry before canonical promotion.


## 0.8 integrated successor — field truth

The installed 0.7.3-dev / versionCode 11 is a **field baseline, not a near-final release**. Target-phone screenshots invalidated its user-facing truth model.

Canonical 0.7.3 field findings:
- Android passive estimates produced misleading 100/100 and 97/100 user-facing scores;
- “Confortable” could contradict the user's real experience;
- 24 h instability could coexist with optimistic current labels;
- before/now deltas could look like causal KINLINK improvement while evidence said no confirmed benefit;
- “Conservateur” wrapped badly;
- mobile data dialog was oversized/verbose;
- MiB was too technical for the normal human UI;
- technical enums leaked into the UI.

Successor line:
- version: 0.8.0-dev / versionCode 12;
- branch: dev/0.8.0-integrated-truth-and-control;
- policy: **no micro-beta chain**;
- next install only after one coherent integrated batch closes exact-head CI, rendered/target-like visual audit, truthfulness gates, signing and Drive readback.

Completion scorecard:
- 45 traceability rows total;
- 32/45 PROVEN-like = 71.1%, but safety/hardening rows dominate and this MUST NOT be called total product completion;
- broad user-facing + architecture weighted maturity = 56.0%;
- improvement-capability weighted maturity = 50.0%;
- strong causal mobile stabilizer remains gated/unbuilt.

Mandatory future takeover:
- read .project-memory/PRODUCT_COMPLETION_SCORECARD.json;
- read .project-memory/VISUAL_COUNTER_AUDIT_POLICY.json;
- automatically counter-audit target screenshots before waiting for the user to enumerate defects.
