# Next Implementation Plan

## Milestone M0 — Repository bootstrap
- Gradle/Kotlin Android project
- CI compile + unit test
- canonical versioning
- static schema validation

## Milestone M1 — Observer Core (first useful runtime slice)
- ConnectivityManager callbacks
- NetworkCapabilities / LinkProperties snapshots
- LAN vs WAN state
- Wi-Fi/mobile context detection
- SQLite event ledger
- cockpit showing current truth state

## Milestone M2 — Mobile Vault
- metered detection
- local byte ledger
- user-defined data envelopes
- hard enforcement hooks
- bundle-exhausted state machine

## Milestone M3 — Diagnostics
- gateway/DNS/Internet micro-probes
- failure-domain inference
- report export
- weekly summary

## Milestone M4 — Stabilizer data plane
- VpnService/TUN backend benchmark
- per-flow classification
- bounded DNS resilience
- fail-open watchdog

## Milestone M5 — Adaptive intelligence
- shadow decisions
- no-regret evaluation
- trusted-context memory
- gradual autopilot promotion
