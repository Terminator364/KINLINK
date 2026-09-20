# KINLINK Canonical Specification v0.6

## Mission

KINLINK is an install-once, low-overhead Android network-resilience autopilot for unstable and expensive connectivity. It separates LAN from WAN, prioritizes Android's authoritative connectivity evidence, applies only bounded reversible actions, protects metered data, and records privacy-safe evidence.

## P0 invariants

1. Never automatically speed-test or probe cellular data.
2. Never automatically probe metered Wi-Fi/hotspots.
3. Never let a failed external probe overrule Android NET_CAPABILITY_VALIDATED.
4. Never call Android connectivity-validation reporting APIs from production KINLINK.
5. Preserve LAN independently from WAN.
6. Never permanently seize Android routing; stronger data planes require fail-open teardown.
7. Preserve the stable signing certificate across upgrades.
8. No root and no paid cloud dependency.
9. Keep RAM, battery and thermal impact bounded.
10. Rate-limit every automatic recovery family.

## Evidence hierarchy

Android VALIDATED, captive portal, active transport and LinkProperties outrank external test endpoints. Micro-probes are supporting evidence. Negative endpoint results are inconclusive unless corroborated; positive results may be used as bounded confirmation.

## RC2 automatic recovery

- event-driven, no polling loop;
- Wi-Fi only;
- unmetered-only automatic confirmation probes;
- healthy validated Wi-Fi is left alone;
- flapping validated Wi-Fi can receive metric refresh only;
- unvalidated Wi-Fi with a local link may receive bounded confirmation;
- positive confirmation remains local evidence only and never alters Android validation state;
- all positive and negative framework connectivity hints are forbidden;
- profile-dependent cooldown/hourly caps;
- battery saver, Android low-memory pressure or severe thermal pressure suspends recovery;
- every executed action generates a bounded local receipt;
- every transport handoff is passively journaled so Wi-Fi→mobile incidents can be reconstructed without routing ownership;
- every recovery action has a hard 5 s fail-open deadline and re-checks that the same Wi-Fi is still active before any post-probe action;
- every transport transition creates a 5 s observation-only settling window so Android completes Wi-Fi/mobile handoff without KINLINK recovery activity.

## Lifecycle

The observer uses Android specialUse foreground-service semantics and restarts after BOOT_COMPLETED or MY_PACKAGE_REPLACED where Android permits.

Lifecycle is fail-open:
- service starts/stops are receipt-carrying;
- more than 3 service creations inside 10 minutes is treated as a restart storm;
- during a restart storm, passive observation remains available but active recovery is suspended;
- no restart storm may cause routing ownership or mobile-data intervention.

## Strong stabilizer

VpnService/TUN remains a separately gated M5 subsystem. It is not production-enabled until DNS resilience, watchdog teardown, RAM/battery, latency and rollback evidence pass.

Compilation alone never qualifies a version as final.


## Mobile handoff observability

- Cellular transport is always observation-only.
- The foreground notification states explicitly that Android retains control on cellular.
- Wi-Fi exit transitions create local handoff receipts.
- A subsequent cellular VALIDATED state records a successful handoff outcome.
- Cellular present but not yet VALIDATED is recorded as evidence only; KINLINK takes no recovery action.


## User fail-open control

KINLINK exposes a persistent Recovery Mode:
- AUTOMATIC: bounded Wi-Fi-only recovery is permitted by the normal policy gates.
- OBSERVATION_ONLY: all active recovery and manual Wi-Fi optimization are suspended; telemetry and status remain available.
- The user can switch modes without uninstalling the app.


## RC3 consolidated safety contract

Active recovery is now governed by one pure policy gate: only AUTOMATIC mode + active Wi-Fi may authorize bounded recovery. Cellular, unknown transport and OBSERVATION_ONLY always block active recovery.

The privacy-safe diagnostic export also carries aggregate handoff and watchdog evidence so a future Wi-Fi→mobile incident can be reconstructed without SSID, SIM identifiers, IP addresses, payloads or forced probes.


## Lifecycle start receipts

BOOT_COMPLETED and MY_PACKAGE_REPLACED start attempts are persisted before the service can disappear from view. The next successful service creation imports that pending receipt into the bounded action ledger. A rejected background foreground-service start is therefore diagnosable later instead of being silently lost.


## Passive quality awareness

KINLINK separates "Android says Internet is validated" from "Android estimates useful capacity".
It records passive downstream/upstream bandwidth estimates from NetworkCapabilities and classifies them as UNKNOWN, CONSTRAINED, LIMITED or COMFORTABLE.

This is a heuristic, not a speed test:
- it consumes no extra mobile data;
- it never overrides Android VALIDATED by itself;
- it never triggers cellular routing;
- it exists to expose slow-but-valid links that the previous cockpit could misleadingly call simply "healthy".
