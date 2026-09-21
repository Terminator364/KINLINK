# KINLINK 0.7.1 — Mobile Assist counter-audit

## Scope

This counter-audit was opened because the broader KINLINK product requirement includes useful cellular resilience, while the earlier safe release had narrowed active recovery to Wi-Fi.

The goal is not to weaken paid-data protection. The goal is to recover useful mobile assistance that remains:
- zero hidden probe traffic;
- non-root;
- non-privileged;
- Android-routing preserving;
- resource bounded;
- budget aware;
- evidence carrying.

## Threat / failure matrix

### MA-001 — hidden cellular probe regression

Risk:
A generic network diagnostic might accidentally call HTTP, DNS or sockets while cellular is active.

Controls:
- MobileAssistController contains no HTTP/DNS/socket primitive;
- candidate-contract statically rejects openConnection, getAllByName, Socket, HttpURLConnection;
- global candidate contract still rejects requestNetwork, bindProcessToNetwork and reportNetworkConnectivity;
- automatic unvalidated mobile state recommends a system panel but the service does not open it automatically.

Status: INTEGRATED / exact-head CI required.

### MA-002 — repeated metric-refresh churn

Risk:
A slow mobile link could generate repeated NetworkCapabilities events and repeatedly request bandwidth updates.

Controls:
- 5-minute automatic cooldown;
- 6 actions/hour cap;
- manual 30-second anti-spam cooldown;
- manual actions share the same action family/hourly accounting;
- two recent UNCHANGED/DEGRADED outcomes suspend further automatic assistance.

Status: INTEGRATED / exact-head CI required.

### MA-003 — stale cellular network race

Risk:
The policy may decide on cellular, then Android may hand off before execution.

Controls:
- controller re-reads activeNetwork and capabilities immediately before requestBandwidthUpdate;
- requires active transport CELLULAR;
- requires VALIDATED;
- requires NOT_SUSPENDED;
- otherwise records ABORT_STALE and applies no action.

Status: INTEGRATED / exact-head CI required.

### MA-004 — resource pressure

Risk:
Even zero-probe assistance can create needless Android/radio work during low RAM, battery saver or severe thermal conditions.

Controls:
- automatic path uses DeviceResourceGuard;
- manual metric refresh also uses resource guard;
- system-panel navigation is still available on explicit user action for unvalidated mobile because navigation itself is not a KINLINK data probe or routing mutation.

Status: INTEGRATED / exact-head CI required.

### MA-005 — budget protection

Risk:
Assistance on paid data must not silently consume the protected reserve.

Controls:
- automatic assistance is blocked for BUNDLE_LOW / BUNDLE_EXHAUSTED / BUNDLE_EXPIRED;
- validated manual metric refresh is blocked under protected budget;
- system-panel navigation may remain available explicitly because KINLINK itself emits no data by opening Android settings;
- no cellular speed test exists.

Status: INTEGRATED / exact-head CI required.

### MA-006 — false benefit attribution

Risk:
A metric refresh could be credited for a later unrelated network improvement.

Controls:
- effectiveness is measured only on the expected CELLULAR transport;
- two subsequent validated observations are required;
- evidence expires after two minutes;
- transport change, stale time window or backward clock makes the result INCONCLUSIVE;
- anti-repeat uses recent unchanged/degraded evidence.

Status: INTEGRATED / exact-head CI required.

### MA-007 — user-facing contradiction

Risk:
The code could assist mobile while cockpit/notification still claim KINLINK only observes.

Controls:
- ConnectivityStateClassifier is cellular-aware;
- passive cause/guidance includes mobile suspended/congestion/low-capacity/unvalidated;
- notification says Mobile Assist while Android keeps routing;
- cockpit has an explicit Mobile Assist button and zero-probe explanation.

Status: INTEGRATED / exact-head CI required.

### MA-008 — inability to quantify mobile bad-link burden

Risk:
KINLINK could react to a current mobile issue without learning whether cellular quality is persistently poor.

Controls:
- passive slow-but-validated mobile episode tracker;
- 24 h cumulative/longest/count evidence;
- reliability summary separates Wi-Fi-lent and Mobile-lent episodes;
- diagnostic TXT/ZIP includes mobile slow-link evidence.

Status: INTEGRATED / exact-head CI required.

## Strong stabilizer boundary

Mobile Assist Level 1 is not the strong data plane.

VpnService/TUN remains separately gated. It must not be enabled until real evidence proves benefit, RAM/battery/latency/DNS/watchdog/rollback/handoff safety.

## Current release decision

The previous signed 0.7.1 artifact was superseded before field delivery.

A new field candidate may exist only after:
1. exact-head design-lint PASS;
2. exact-head Android unit tests/lint/build PASS;
3. Mobile Assist candidate-contract PASS;
4. exact artifact hash/package/version proof;
5. fresh stable signing with the canonical certificate;
6. Drive FIELD_CANDIDATE readback hash proof.

No user install before those gates.
