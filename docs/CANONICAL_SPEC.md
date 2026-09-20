# KINLINK Canonical Specification v0.5

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
- battery saver or severe thermal pressure suspends recovery;
- every executed action generates a bounded local receipt;
- every transport handoff is passively journaled so Wi-Fi→mobile incidents can be reconstructed without routing ownership.

## Lifecycle

The observer uses Android specialUse foreground-service semantics and restarts after BOOT_COMPLETED or MY_PACKAGE_REPLACED where Android permits.

## Strong stabilizer

VpnService/TUN remains a separately gated M5 subsystem. It is not production-enabled until DNS resilience, watchdog teardown, RAM/battery, latency and rollback evidence pass.

Compilation alone never qualifies a version as final.
