# KINLINK implementation plan — consolidated

## Completed foundation

### M0 — Repository / CI / evidence discipline
- Gradle/Kotlin Android baseline
- CI unit tests + APK build
- canonical state/checkpoint files
- deterministic artifact hashes

### M1 — Observer Core
- ConnectivityManager callbacks
- NetworkCapabilities / LinkProperties reduction
- LAN vs WAN separation
- bounded local SQLite ledger
- cockpit UI
- privacy-safe diagnostic export

### M2 — Stable update baseline + first bounded action
- persistent production signing certificate
- Android 15/16 foreground-service hardening
- explicit Wi-Fi-only micro-probe
- Android connectivity re-evaluation hook
- bandwidth-metric refresh request

### M3 — Resilience Evidence Engine
- Android VALIDATED outranks isolated probe failure
- two-endpoint bounded fallback
- captive-portal priority
- coherent positive/negative report policy
- field contradiction converted into permanent regression tests

### M4 — Mobile Vault / Cost Guard — IMPLEMENTED IN RC1
- passive device-wide mobile RX/TX delta tracking
- reboot/counter-reset safe baseline
- optional persisted daily envelope
- BUNDLE_OK / LOW / EXHAUSTED policy
- KINLINK probe/retry hold at LOW and EXHAUSTED
- no continuous polling
- duplicate UI/service ledger writes removed

### M6 — Adaptive Intelligence, safe subset — IMPLEMENTED IN RC1
- bounded 15-minute stability window
- instability score and flapping flag
- no-regret adaptive policy
- healthy Wi-Fi left alone
- unstable-but-validated Wi-Fi observed without hidden probe
- budget state overrides recovery
- captive portal remains user-driven

## M5 — Stabilizer Data Plane — GATED, NOT YET PROMOTED

Before activation:
- VpnService/TUN prototype
- DNS resilience/cache benchmark
- per-flow classification
- fail-open heartbeat/watchdog
- no-lockdown default
- battery/RAM/latency benchmark against plain Android
- automatic disable when no measurable benefit exists
- field rollback test

Android ConnectivityDiagnostics callbacks are not assumed available to a normal app; KINLINK must first qualify as an active connectivity provider such as a VPN before relying on them.

## Current integrated candidate

0.4.0-rc1 combines M3 + M4 + safe M6 behavior. It is intentionally more complete than the previously installed M2 and is built as one upgrade candidate.

## Final product gate

Do not label KINLINK final/total merely because a milestone compiles.
The final gate requires the integrated candidate to pass CI, stable-signing continuity, Drive readback, update-in-place installation, field behavior, and any future M5 data-plane benchmarks.
