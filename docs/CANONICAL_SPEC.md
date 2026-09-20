# KINLINK Canonical Specification v0.3

## Mission

KINLINK is a low-overhead Android network-resilience autopilot for unstable and expensive connectivity. It observes the real network state, separates LAN health from WAN health, arbitrates contradictory evidence, applies only bounded reversible actions, protects paid mobile data, preserves local-network operation, and records privacy-safe evidence for continuous hardening.

The target is not a diagnostic toy. The target product is an install-once network companion whose normal operating mode is automatic, conservative and fail-open.

## Product invariants — P0

1. Never spend paid mobile data for automatic speed tests or volumetric probes.
2. Never let a single external test endpoint overrule Android NET_CAPABILITY_VALIDATED.
3. Never classify an unreachable diagnostic endpoint as proof that the ISP or Wi-Fi is broken.
4. Never permanently seize Android routing. Any stronger data plane must have immediate fail-open teardown.
5. Preserve LAN usability when WAN is unavailable.
6. Never rotate the production signing certificate after the stable baseline without an explicit migration plan.
7. No cloud/VPS dependency for normal operation.
8. No root requirement.
9. Battery, thermal and RAM overhead remain bounded.
10. Any optimization stronger than observation must have a rollback path and evidence threshold.

## Evidence hierarchy

Connectivity truth is multi-signal and confidence-weighted.

Highest-confidence public signals:
- Android validated Internet capability;
- Android captive-portal capability;
- active transport and LinkProperties.

Supporting evidence:
- bounded user-initiated micro-probes;
- DNS/gateway observations;
- historical transition evidence;
- future ConnectivityDiagnostics events.

A negative micro-probe is supporting evidence only. If Android already reports a validated network, a failed probe is recorded as endpoint-specific/inconclusive, not as WIFI_BAD.

## Connectivity dimensions

KINLINK maintains independent state for:
1. transport availability;
2. local-link/LAN state;
3. Internet validation;
4. captive portal;
5. failure domain;
6. metered/mobile budget state;
7. user/application intent;
8. battery/thermal budget;
9. diagnostic confidence;
10. last bounded recovery action and outcome.

## Bounded action ladder

- A0 Observe — callbacks only, no packet generation.
- A1 Refresh — request fresh bandwidth/network metrics.
- A2 Confirm — explicit Wi-Fi-only micro-probe with endpoint fallback.
- A3 Revalidate — report coherent positive/negative evidence to Android only when Android is not already authoritative.
- A4 Stabilizer — optional VpnService/TUN data plane, disabled until its own benchmark and fail-open gates pass.
- A5 Adaptive autopilot — shadow evaluation first; promotion only after no-regret evidence.

## User-visible profiles

- Conservative
- Balanced (default)
- Maximum Stability

The user should not need to tune internal thresholds manually.

## Total-product acceptance gate

A version may be called product-grade only when all applicable gates pass:
- compile + unit/regression tests;
- stable signing;
- install/update continuity;
- Android 15/16 lifecycle behavior;
- no contradictory state presentation;
- mobile-data safety;
- LAN/WAN separation;
- bounded recovery behavior;
- fail-open watchdog for any data plane;
- privacy-safe diagnostics;
- field evidence from the target phone;
- rollback/update path.

Source exists or APK compiles is never enough evidence for a final claim.
