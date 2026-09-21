# KINLINK W2 — plan usage evidence and reconciliation

This block closes a major semantic hole between Android's device-wide counters and the Mobile Vault's per-plan economic zones.

## Rules

1. Device-wide mobile bytes are **not** automatically a single-plan truth.
2. User-reconciled, carrier-adapter or attributable optional NetworkStats evidence can be PLAN_EXACT.
3. Device-wide aggregate-only evidence holds the plan zone at UNKNOWN instead of spending from a guessed balance.
4. Conflicting exact sources are preserved as CONFLICT/HOLD; KINLINK never averages them into fake certainty.
5. Close exact sources use the conservative maximum used-bytes value.
6. Counter rollback/reboot starts a new aggregate counter generation but never decreases already proven cycle usage.
7. Mobile plan configuration now optionally stores a cycle-start timestamp and remains versioned. Schema v1 reads remain accepted with cycleStart=null; new writes use schema v2.

This implements the safety intent behind B91-B100 without requesting privileged telephony identifiers or making Usage Access a core dependency.
