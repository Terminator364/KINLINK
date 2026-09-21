# KINLINK — NEW CONVERSATION TAKEOVER

## Exact code to type

`KINLINKGO`

This is the only canonical takeover code.

## What the receiving conversation must do

On seeing exactly `KINLINKGO`, it must **not** start with a generic explanation, a reconstruction request, or a long ChatGPT report.

It must first load durable KINLINK state from GitHub and resume from the latest proven checkpoint.

Mandatory reads:
- `project_state.json`
- `NEXT_UNCOMMITTED_ACTION.md`
- `BUILD_STATUS.md`
- `docs/CANONICAL_SPEC.md`
- `docs/KINLINK_CONTINUITY.md`
- `.project-memory/NEW_CONVERSATION_TAKEOVER.json`
- `.project-memory/COMMUNICATION_PROTOCOL.json`
- `.project-memory/COMMUNICATION_STATE_MACHINE.json`
- `.project-memory/ACTIVE_TRANCHE.json`
- `.project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl`
- `.project-memory/FIELD_CANDIDATE_FREEZE.json`

## Communication continuity is part of product continuity

The receiving conversation must preserve the existing protocol, not merely the code state.

Normal tranche:
- 25 minutes total;
- 22 minutes useful work;
- 3 minutes reserved for **normal primary close**;
- Gmail START before substantive work;
- provider ACK/message ID persisted;
- Gmail END before final app reply;
- provider ACK/message ID persisted;
- KINLINK Gmail label;
- app response short when Gmail succeeded.

Watchdogs are post-nominal backups only. They must never become the ordinary close path.

Before new work, an unclosed prior tranche must be reconciled by delivery key + Gmail search to prevent duplicate END mail.

## Work style that must survive chat migration

- no restarting KINLINK from zero;
- no micro-beta chain;
- large coherent engineering batches;
- exact-head CI only;
- no inherited PASS from another SHA;
- no premature install request;
- no APK by Gmail;
- minimum manual user actions;
- test/audit/counter-audit before field delivery;
- keep canonical rollback intact until Stage B promotion proof.

## P0 UI rule learned from 0.7.2

The installed 0.7.2 proved that compile/lint success is not enough for UI release quality.

Before a new candidate can reach the user:
1. responsive layout must be rendered/tested before delivery;
2. normal and large font scales must pass;
3. portrait width must pass;
4. header must never collapse into one-character columns;
5. every button must be tested for position, width, wrapping, relevance and tap target;
6. Wi-Fi/mobile/safe-mode/resource-constrained/degraded states must be visually checked;
7. technical detail stays collapsed by default;
8. absurd whitespace, overflow, clipping or overlapping is a release blocker.

## Current true product state

Installed:
- KINLINK 0.7.2 / versionCode 10;
- functional but **P0 UX-invalid** as a near-final candidate because target-phone screenshots proved responsive header compression.

Canonical rollback:
- 0.6.0-rc3 / versionCode 7 remains canonical.

Current engineering line:
- branch: `dev/0.7.3-final-like-ui-control`
- exact head: `6ef270d83461628aa8a6a6356b0d56dece6f0ca1`
- design-lint `35616879792`: PASS
- Android `35616879783`: PASS

0.7.3 integrated direction:
- full-width vertical header fix;
- before/current/delta control proof card;
- maintained-duration + relapse states;
- current-now vs 24h history separation;
- explicit Conservateur / Stable / Max selector;
- profile-aware bounded mobile vigilance;
- exact battery/thermal/low-memory reason;
- responsive UI static regression gate.

## Exact next action after takeover

1. final counter-audit exact 0.7.3 head;
2. responsive/emulator UI proof before field delivery;
3. reject 0.7.3 if any layout state is visually unacceptable;
4. if clean: freeze exact source;
5. sign with canonical signer;
6. stage to Drive FIELD_CANDIDATE;
7. read back and hash-verify;
8. only then request one in-place install.

## Mobile improvement truth

KINLINK Level 1 can continuously observe, detect degradation, perform bounded allowed actions, verify sustained correlated improvement, detect relapse, and re-evaluate later.

It must **not** claim that Android `requestBandwidthUpdate()` itself accelerates carrier throughput.

A future strong stabilizer may only be promoted after causal-benefit evidence plus RAM/battery/thermal/latency/DNS/watchdog/rollback gates.

## Canonical resume code

`KINLINKGO`
