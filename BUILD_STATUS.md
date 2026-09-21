# KINLINK build status

## Field baseline installed
- KINLINK **0.7.3-dev / versionCode 11**
- installed/usable baseline only; not near-final
- no new phone install requested

## Full A+B+C scope
- macro capabilities: **80**
- conservative maturity: **45.1%**
- B-depth: **B31-B100**
- program: W0-W6
- **W2 Context + Mobile Vault active**
- no user-facing micro-beta chain

## Communication / continuity
- resume capsule + cold-takeover verifier active
- one watchdog only: **KINLINK Comms Watchdog**
- CLOSE_INTENT without END ACK = P0
- watchdog stale-WORKING threshold: **28 min**
- foreground protocol remains 22 min work + 3 min close reserve

## 0.8 proven foundations
- b540 rendered UI/truth: 45/45 manually audited PASS
- e633 release provenance: 0.8.0-dev / v12 exact readback PASS
- f2a6393 W2 plan store: design-lint + Android CI PASS
- 6f043ab W2 usage reconciliation/UI: design-lint + Android CI PASS

## W2 usage reconciliation
Implemented:
- per-plan exact vs device-mobile-aggregate attribution
- conflicts HOLD instead of averaging
- reboot/counter reset preserves proven cycle usage
- local usage evidence store
- plan store schema v2 with optional cycle start
- no privileged SIM identifiers
- no Usage Access dependency for core correctness

## Visual counter-audit finding
Manual review of all **45** renders from 6f043ab found one blocking XL-font defect:
- Mobile Vault dialog pushed **EFFACER** below the viewport.

No maturity score increase was granted from that head.

## Active exact successor
- branch: `dev/0.8.0-integrated-truth-and-control`
- head: `7d589ddd6038adcb7c318f2b00dc1cf03faaa355`
- fix: compact Mobile Vault dialog + scrollable in-form clear action
- design-lint `35664821268`: **PASS**
- Android `35664821258`: **IN PROGRESS at close intent**

Next: inspect fresh 45-shot proof if CI passes. No signing/Drive staging/phone install yet.
