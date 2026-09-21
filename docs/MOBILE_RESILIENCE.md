# KINLINK Mobile Resilience

## Product requirement

KINLINK is not Wi-Fi-only. It must also improve the user's experience when Android's active default transport is cellular, especially in unstable/expensive-network conditions.

The implementation is split into two safety levels so "mobile optimization" never means hidden paid traffic or unsafe modem/routing control.

## Level 1 — Mobile Assist (current 0.7.1 line)

Mobile Assist works without root, without a VPN data plane and without cellular speed tests.

It may:
- classify passive cellular degradation from Android NetworkCapabilities;
- distinguish validated-but-low-capacity, congestion-suspect, suspended and unvalidated mobile states;
- request a bounded Android bandwidth-metric refresh on validated degraded cellular;
- rate-limit automatic refreshes to a 5-minute cooldown and 6/hour maximum;
- stop after two recent UNCHANGED/DEGRADED outcomes;
- suppress itself under low-memory / battery-saver / severe thermal pressure;
- suppress itself when the configured mobile budget is LOW / EXHAUSTED / EXPIRED;
- on an explicit user tap, open Android's Internet connectivity panel when the mobile network is not validated;
- record privacy-safe action/outcome receipts.

It must never:
- run an automatic cellular HTTP/DNS probe;
- run a mobile speed test;
- call requestNetwork for paid mobile recovery;
- bind KINLINK or the device to a cellular route;
- toggle mobile data or airplane mode using privileged/hidden APIs;
- report negative connectivity back to Android;
- override Android VALIDATED;
- silently spend data to "test" whether mobile is working.

### What Level 1 can and cannot improve

Level 1 can make KINLINK materially more intelligent on cellular:
- detect that the mobile link is slow/congested/suspended;
- stop calling a slow-but-validated mobile link simply "healthy";
- avoid useless repeated work;
- ask Android to refresh link metrics without test traffic;
- guide the user to the exact Android connectivity control only when system intervention is needed.

A normal non-root Android app cannot directly force the modem to a stronger tower, toggle cellular data silently, or rewrite system-wide routing. KINLINK must not pretend otherwise.

## Level 2 — Strong Mobile Stabilizer (gated)

The broader product specification still includes an optional VpnService/TUN data plane for device-wide resilience work such as:
- DNS resilience;
- safe per-flow routing policy;
- flow classification;
- bounded shaping/admission controls;
- fail-open watchdog teardown.

This is not production-enabled yet.

It may only graduate from SHADOW/GATED status if evidence proves:
1. user VPN consent;
2. measurable benefit on real bad mobile links;
3. bounded RAM on low-memory devices;
4. bounded battery/thermal cost;
5. bounded added latency;
6. correct DNS behavior;
7. watchdog teardown;
8. no connectivity lockout on crash/restart;
9. rollback to Android-native networking;
10. no regression of Wi-Fi/mobile handoff;
11. Mobile Vault budget compliance.

## Current release rule

0.7.1 may include Mobile Assist Level 1 because it does not seize routing or emit hidden cellular probes.

Level 2 remains separately gated and must never be smuggled into a release merely because the code compiles.
