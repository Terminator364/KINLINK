# KINLINK Canonical Specification v0.4

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
11. One network transition must be recorded once; UI observers may not duplicate background-service ledger writes.

## Evidence hierarchy

Connectivity truth is multi-signal and confidence-weighted.

Highest-confidence public signals:
- Android validated Internet capability;
- Android captive-portal capability;
- active transport and LinkProperties.

Supporting evidence:
- bounded user-initiated Wi-Fi micro-probes;
- historical transition evidence;
- local instability score;
- mobile byte counters;
- future data-plane diagnostics only when Android actually exposes them to KINLINK.

A negative micro-probe is supporting evidence only. If Android already reports a validated network, a failed probe is endpoint-specific/inconclusive, not WIFI_BAD.

## Mobile Vault

Mobile Vault is local-first and passive by default:
- TrafficStats mobile RX/TX counters are sampled only when KINLINK already receives an event or renders the cockpit;
- counters are persisted as deltas, never polled continuously;
- daily envelope is optional and user-defined;
- 80% of the configured envelope enters BUNDLE_LOW;
- 100% enters BUNDLE_EXHAUSTED;
- BUNDLE_LOW/EXHAUSTED suspend KINLINK mobile probes and retries;
- a counter rollback after reboot resets the baseline without inventing traffic;
- no carrier credential, SIM identifier or packet payload is stored.

TrafficStats is a guardrail estimate, not carrier billing truth. Carrier balance/expiry integration remains a separate evidence source.

## Stability and anti-flapping

KINLINK keeps a bounded recent event window and derives:
- number of state events;
- Internet-state transitions;
- validated-event ratio;
- 0..100 instability score;
- flapping flag.

The adaptive policy is no-regret:
- validated stable Wi-Fi is left alone;
- unstable but still validated Wi-Fi is observed, not aggressively reset;
- low/exhausted mobile budget overrides recovery;
- captive portals require user action;
- incomplete evidence defaults to observation.

## Android platform constraints

ConnectivityDiagnosticsManager is not treated as a guaranteed signal for the current app role. Android documents that callbacks are delivered only to apps that offer connectivity to the user, such as active VPNs, carrier apps or Wi-Fi suggesters, with relevant permissions. KINLINK therefore does not claim these callbacks until the future stabilizer data plane qualifies and is separately verified.

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
10. bounded recovery action and outcome;
11. recent instability/flapping evidence.

## Bounded action ladder

- A0 Observe — callbacks only, no packet generation.
- A1 Refresh — request fresh bandwidth/network metrics.
- A2 Confirm — explicit Wi-Fi-only micro-probe with endpoint fallback.
- A3 Revalidate — report coherent positive/negative evidence to Android only when Android is not already authoritative.
- A4 Stabilizer — optional VpnService/TUN data plane, disabled until benchmark and fail-open gates pass.
- A5 Adaptive autopilot — current shadow/no-regret policy; active promotion only after field evidence.

## User-visible profiles

- Conservative
- Balanced (default)
- Maximum Stability

The user should not have to tune internal thresholds individually.

## Total-product acceptance gate

A version may be called product-grade only when all applicable gates pass:
- compile + unit/regression tests;
- stable signing;
- install/update continuity;
- Android 15/16 lifecycle behavior;
- no contradictory state presentation;
- no duplicate telemetry;
- mobile-data safety and budget guard;
- LAN/WAN separation;
- bounded recovery behavior;
- anti-flapping behavior;
- fail-open watchdog for any data plane;
- privacy-safe diagnostics;
- field evidence from the target phone;
- rollback/update path.

Source exists or APK compiles is never enough evidence for a final claim.
