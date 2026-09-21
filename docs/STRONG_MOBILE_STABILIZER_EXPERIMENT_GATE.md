# Strong Mobile Stabilizer — experiment gate

## Why this remains gated

Android documents `ConnectivityManager.requestBandwidthUpdate(Network)` as a request for updated bandwidth information. It does not itself increase radio throughput.

Android `VpnService` can create a virtual IP interface and process device traffic, which is powerful enough to implement a real traffic-handling layer, but that also creates new failure, latency, RAM, battery and DNS risks.

References:
- https://developer.android.com/reference/android/net/ConnectivityManager#requestBandwidthUpdate(android.net.Network)
- https://developer.android.com/reference/android/net/VpnService

## Zero-dollar realistic benefit classes

A local-only strong stabilizer can plausibly improve **experience**, not carrier physics, by experimentally testing:
- DNS response resilience/cache behavior;
- foreground-flow priority over background bulk traffic;
- bounded queueing and admission under very constrained links;
- fast fail-open detection and teardown;
- optional per-flow route choice when multiple Android networks are legitimately available.

It cannot honestly promise:
- stronger cellular signal;
- a better tower;
- more carrier capacity;
- seamless whole-device multipath bonding without an aggregation anchor;
- free bandwidth.

## Experiment sequence

### S0 — shadow measurements
No VPN. Reuse KINLINK evidence to capture:
- passive quality score;
- interruptions;
- low-quality duration;
- resource cost.

### S1 — local TUN safety skeleton
User-consented VpnService only.
No optimization claim.
Required:
- establish/close;
- watchdog;
- crash teardown;
- DNS correctness;
- no traffic black-hole;
- immediate Android-native fallback.

### S2 — one mechanism at a time
Candidate mechanisms must be isolated. No “kitchen sink” VPN.
Each experiment receives:
- explicit mechanism ID;
- baseline window;
- treatment window;
- rollback;
- resource receipt.

### S3 — benefit qualification
A mechanism graduates only if repeated field evidence shows:
- lower interruption burden, latency proxy or foreground starvation;
- no significant data amplification;
- no battery/thermal block;
- no RAM-growth block;
- no handoff regression.

### S4 — production gate
Only after S3 may the UI expose a strong stabilizer as an optional feature.

## Current decision

Do not enable VpnService in 0.7.2 merely to look more “final”.
The correct near-final product is:
- honest Level-1 continuous care now;
- strong stabilizer developed behind a separate experimental gate;
- no unverified routing ownership.
