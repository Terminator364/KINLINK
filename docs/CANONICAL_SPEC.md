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


## Bounded Wi-Fi responsiveness

The explicit "Optimiser le Wi-Fi maintenant" path may reuse its existing Wi-Fi-only 204 micro-probe latency to classify responsiveness as RESPONSIVE, SLOW or VERY_SLOW.

This classification:
- adds zero extra requests beyond the already bounded manual micro-probe;
- is not a throughput/speed test;
- never runs on cellular;
- never overrides Android validation by itself;
- is diagnostic evidence only.


## Passive-quality hysteresis

Low passive capacity must persist for 3 consecutive VALIDATED Wi-Fi observations before it may request a metrics refresh.
A single low estimate never triggers recovery.
The action remains REFRESH_METRICS only: no hidden probe, no route change, no mobile assist.


## Probe handoff abort

The bounded Wi-Fi micro-probe re-checks before each endpoint that the originally captured network is still Android's active Wi-Fi.
If a handoff started, remaining endpoints are skipped immediately.
The probe never falls through to cellular.


## Low-noise quality history

Telemetry schema v3 stores only the passive quality tier with each deduplicated network event.
It does not persist raw per-callback kbps fluctuations.
Existing v2 databases migrate in place with UNKNOWN as the historical default.


## Metered Wi-Fi protection

Explicit manual optimization also respects metered Wi-Fi.
When Android marks the active Wi-Fi as metered, KINLINK performs no HTTP micro-probe and only requests a passive bandwidth-metric refresh.
This protects tethered/hotspot data plans from hidden probe traffic.


## Recovery effectiveness receipts

After an automatic metrics refresh, KINLINK passively compares the triggering Wi-Fi quality tier with the quality after two subsequent validated Wi-Fi observations.
It records IMPROVED, UNCHANGED, DEGRADED or INCONCLUSIVE.
No extra network request is generated for this evaluation.


## Anti-repeat ineffective recovery

Within the bounded one-hour action window, two UNCHANGED or DEGRADED recovery outcomes suspend further automatic recovery attempts.
This prevents KINLINK from repeatedly refreshing metrics when evidence shows no benefit.


## Passive failure-cause classification

KINLINK classifies likely causes without generating traffic:
NO_LINK, CAPTIVE_PORTAL, DNS_CONFIGURATION_SUSPECT, LOW_CAPACITY, FLAPPING, WAN_UNVALIDATED, MOBILE_UNVALIDATED, NONE or UNKNOWN.

DNS_CONFIGURATION_SUSPECT is deliberately cautious: it means a local Wi-Fi link exists, Internet is unvalidated and Android exposes zero DNS servers. It is evidence, not proof of a DNS outage.


## Passive interruption evidence

After Internet was previously VALIDATED, KINLINK passively timestamps loss of validation and records duration only when VALIDATED returns.
Severity:
- MICRO < 2 s
- SHORT 2–30 s
- LONG >= 30 s

No timer, probe or packet is generated to measure the outage. Initial unknown startup state never invents an interruption.


## Cause-aware passive guidance

Every passive problem cause maps to a non-invasive user guidance message.
Cause transitions are journaled only when the cause changes, avoiding repeated log noise.
Guidance never triggers routing, DNS replacement, mobile-data toggling or hidden probes.


## Cause-aware notification and history

The foreground notification reflects the current passive cause on Wi-Fi (limited capacity, captive portal, DNS configuration suspicion, WAN unvalidated, flapping) without creating traffic.
Diagnostic exports aggregate passive cause transitions from the bounded action ledger.


## Session health synthesis

KINLINK produces a qualitative session-health state: HEALTHY, WATCH, DEGRADED or CRITICAL.
It combines current Internet state, passive cause and recent instability/flapping.
No opaque numeric score is exposed as a claim of objective network quality.


## Post-update runtime self-test

Once per versionCode, service startup performs a zero-network core self-test:
- telemetry database opens at schema >= 3;
- RecoveryMode is readable.

The first real NetworkCallback event then records observer-callback self-test PASS.
A failed core test is not marked complete, so evidence remains visible and can be retried on a later service creation.


## Runtime resource qualification hooks

Each service session captures a lightweight start/end resource snapshot:
- process PSS in MiB;
- Android battery percentage when available;
- elapsed session duration.

Battery drain per hour is only calculated for sessions >= 30 minutes; shorter sessions are explicitly inconclusive.
These receipts are qualification evidence, not a claim that all battery drain belongs to KINLINK.


## Background notification dedupe

The foreground notification is re-published only when its visible text changes.
Repeated equivalent NetworkCallback events do not trigger redundant NotificationManager updates.


## Mobile counter sampling discipline

Device-wide Android mobile TrafficStats are cached for 15 seconds per epoch day.
Repeated network callbacks inside that window reuse the cached snapshot.
Changing the configured prudence threshold invalidates the cache immediately.
No polling loop is introduced.


## Telemetry retention

Privacy/storage bounds are enforced by both age and row count:
- network events: max 7 days and max 5,000 rows;
- action receipts: max 14 days and max 500 rows.

Pruning occurs only when KINLINK already writes an event/receipt; no maintenance polling job is added.


## Structured interruption duration

Telemetry schema v4 adds an optional duration_ms field to action receipts.
Interruption receipts store their measured duration structurally.
Diagnostics report cumulative and longest retained interruption duration.
Existing receipts migrate with NULL duration and remain valid.


## Recovery action control-path timing

Automatic recovery receipts now store the elapsed execution duration of the KINLINK action path.
This is explicitly not labeled as network latency.

Duration classes:
- FAST <= 250 ms
- BOUNDED >250 ms and <= half watchdog
- NEAR_WATCHDOG > half watchdog and <= watchdog
- OVER_WATCHDOG > watchdog

After a micro-probe, if more than half of the 5 s watchdog budget is already consumed, KINLINK freezes further action and does not issue an additional bandwidth refresh.


## Rolling reliability windows

Diagnostics expose 1-hour and 24-hour interruption burden:
- interruption count;
- cumulative interruption duration;
- longest interruption;
- dominant passive-cause transition in the last 24 hours.

A qualitative burden is derived as QUIET / NOTICEABLE / UNSTABLE / SEVERE.
KINLINK deliberately does not infer an availability percentage from sparse Android callbacks.


## Automatic recovery safety circuit breaker

Within a 30-minute window, two safety aborts (watchdog, transport changed, network/capabilities disappeared) open a circuit breaker.
While open, Autopilot performs no automatic recovery action and falls back to passive observation.
The breaker naturally closes as old abort receipts age out of the window.


## Cockpit 24-hour reliability visibility

While the app UI is open, technical details show the latest 24-hour interruption window:
- interruption count;
- cumulative duration;
- longest interruption;
- qualitative burden;
- dominant passive cause transition when available.

This uses local SQLite evidence only and creates no network traffic.


## Slow-but-validated Wi-Fi episode evidence

KINLINK now measures continuous Wi-Fi periods where Android still reports VALIDATED but passive quality is LIMITED or CONSTRAINED.
The episode ends when quality returns to COMFORTABLE/UNKNOWN or the active transport changes.
Receipts store duration_ms and worst observed severity.
No probe or polling loop is added.


## 24-hour slow-link burden

The 24-hour reliability window now includes completed slow-but-validated Wi-Fi episodes:
- episode count;
- cumulative duration;
- longest episode.

This is reported separately from hard Internet interruptions so "connected but painfully slow" is not hidden inside an availability-only view.
