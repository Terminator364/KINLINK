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

## M3 — Resilience Evidence Engine — CURRENT

Deliver as one coherent upgrade:
- Android VALIDATED outranks single-probe failure
- two-endpoint bounded fallback probe
- captive-portal priority
- coherent positive/negative report policy
- explicit regression test for the field contradiction
- clearer human explanation and technical evidence
- no automatic mobile probe

## M4 — Mobile Vault / Cost Guard

- persisted user data envelope
- device/mobile byte-delta ledger with reboot-safe counter reset handling
- low/exhausted/expired policy state
- hard prevention implemented only where Android public APIs/VpnService can guarantee it
- zero hidden speed tests/retry storms
- per-action estimated data cost

## M5 — Stabilizer Data Plane

- VpnService/TUN backend prototype
- DNS resilience/cache benchmark
- per-flow classification
- fail-open heartbeat/watchdog
- no-lockdown default
- battery/RAM/latency benchmark against plain Android
- automatic disable when no measurable benefit exists

## M6 — Adaptive Intelligence

- trusted-network context memory
- shadow decisions
- hysteresis and anti-flapping
- no-regret evaluation
- gradual autopilot promotion

## Final product gate

Do not label KINLINK final/total merely because a milestone compiles.
The total product gate requires M0–M6 applicable safety/evidence gates, stable update continuity, and field-verified behavior on the target phone.
