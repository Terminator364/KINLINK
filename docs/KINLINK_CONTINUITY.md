# KINLINK continuity

## Canonical recovery command

`KINLINKGO`

## Resume procedure

1. Open `Terminator364/KINLINK`.
2. Reload:
   - `project_state.json`
   - `NEXT_UNCOMMITTED_ACTION.md`
   - `BUILD_STATUS.md`
   - `docs/CANONICAL_SPEC.md`
   - `docs/KINLINK_CONTINUITY.md`
   - `.project-memory/FIELD_CANDIDATE_FREEZE.json`
   - `.project-memory/RELEASE_READINESS_0_7_2.json`
   - `.project-memory/COMMUNICATION_PROTOCOL.json`
   - `.project-memory/COMMUNICATION_STATE_MACHINE.json`
   - `.project-memory/ACTIVE_TRANCHE.json`
   - `.project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl`
3. Read exact Git heads and exact-head CI; never inherit green status from another SHA.
4. Resume from `next_uncommitted_action`; never restart M0/M1.
5. Preserve frozen release branches and Drive hashes.

## Communication v2 — mandatory

Cadence:
- 22 min useful work;
- 3 min primary normal close reserve.

Normal close owner:
`PRIMARY_ASSISTANT`

START:
1. reconcile any prior incomplete tranche;
2. send Gmail START;
3. require provider message ID + thread ID;
4. apply `KINLINK` label;
5. persist a unique delivery key;
6. begin work.

END:
1. minute 22: stop new product mutations;
2. persist exact technical checkpoint + CLOSE_INTENT;
3. send normal FIN as a reply in the START Gmail thread;
4. require provider END message ID;
5. apply `KINLINK` label;
6. persist END_ACKNOWLEDGED/CLOSED;
7. only then app pointer.

Post-nominal redundancy:
- backup cannot preempt normal close;
- backup first searches Gmail thread + delivery key;
- if END exists, persist its receipt without resend;
- if absent, send the same normal FIN semantics;
- user-visible “RECOVERY/RATTRAPAGE/WATCHDOG” subject wording is forbidden for ordinary tranche close.

APK:
- never by Gmail;
- Drive KINLINK only.

## Durable product state

Canonical rollback:
- `0.6.0-rc3` / versionCode 7;
- `KINLINK/INSTALLER/KINLINK_LATEST.apk` unchanged.

Installed phone:
- `0.7.1` / versionCode 9;
- usable;
- superseded before canonical promotion by the consolidated 0.7.2 candidate.

Current frozen final-like field candidate:
- `0.7.2` / versionCode 10;
- immutable source branch: `release/0.7.2-field-candidate`;
- exact source: `cc48c1dea1a151af25edc1942bb4efa983c5ff13`;
- design-lint `35610708971`: PASS;
- design-lint `35610716886`: PASS;
- Android candidate `35610708876`: PASS;
- signed SHA-256:
  `a67537e814f81230526ff5de211bdfcd6d810831fcaed75fdc1252578e5b364b`;
- signer cert:
  `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`;
- Drive file ID:
  `1pBf93qjb9vE3T3BIW7Jg54DuSIch3wSU`;
- Drive readback: PASS.

0.7.2 major delta:
- compact status/action/proof cockpit;
- contextual primary action;
- passive quality without speedtest;
- truthful metric-refresh semantics;
- sustained correlated-benefit evidence;
- transient and post-sustained relapse;
- bounded one-shot follow-up;
- healthy-path CPU/SQLite reduction;
- moderate-thermal active-work suppression;
- inherited fail-open/data-cost/privacy protections.

Drive FIELD_CANDIDATE:
- current 0.7.2 candidate at root;
- installed 0.7.1 history in `INSTALLED_SUPERSEDED_0.7.1`;
- older 0.7.0 history in `INSTALLED_SUPERSEDED_0.7.0`.

## Current true action

One in-place update to exact 0.7.2 is the next field gate.

Do not uninstall 0.7.1 first.

After versionCode 10 install:
- core self-test;
- observer callback self-test;
- Wi-Fi -> validated cellular;
- cellular -> Wi-Fi return;
- >=30-minute resource qualification;
- continuous Mobile Assist evidence/relapse field receipts;
- target-phone visual cockpit validation.

Canonical INSTALLER promotion remains forbidden until Stage B PASS.

## Strong stabilizer

Experiment branch:
`experiment/strong-mobile-stabilizer-s0`

The strong VpnService/TUN path remains gated behind:
- user VPN consent;
- DNS correctness;
- watchdog/crash teardown;
- handoff safety;
- resource qualification;
- latency regression gate;
- no data amplification;
- repeated measured benefit.

No unverified VPN code is allowed into the frozen 0.7.2 field candidate.

## Short code

`KINLINKGO`


## New-conversation hard handoff

When a fresh conversation receives `KINLINKGO`, it must first load:
- `.project-memory/NEW_CONVERSATION_TAKEOVER.json`
- `docs/KINLINK_NEW_CONVERSATION_TAKEOVER.md`

This handoff preserves both engineering state **and** the Gmail/25-minute communication protocol.

The receiving conversation must not ask the user to reconstruct prior context, must not restart from zero, and must not replace the normal Gmail-first/Gmail-last workflow with a long ChatGPT explanation.

Current exact 0.7.3 engineering head:
`6ef270d83461628aa8a6a6356b0d56dece6f0ca1`

Exact-head CI:
- design-lint `35616879792`: PASS
- Android `35616879783`: PASS

Next gate:
final counter-audit + responsive/emulator UI proof before any new field install.
