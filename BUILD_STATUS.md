# KINLINK build status

## Installed field baseline
- 0.6.0-rc3
- bidirectional Wi-Fi → cellular → Wi-Fi field gate: PASS
- original mobile-block symptom: NOT REPRODUCED
- canonical Drive artifact remains the installed RC3 baseline

## Post-field development batch
Integrated after the field gate:
- suppress transient OFFLINE flash for 1.5 s during default-network handoff;
- correct mobile-counter wording (Android device-wide TrafficStats observed by KINLINK, not KINLINK app usage);
- capture Android passive down/up bandwidth estimates;
- classify passive link quality independently from Android VALIDATED;
- Autopilot no longer calls a constrained VALIDATED Wi-Fi simply steady;
- explicit manual Wi-Fi micro-probe now reports bounded responsiveness (responsive/slow/very slow) with no extra requests;
- diagnostic report includes passive capacity/quality;
- telemetry fingerprints use quality tier instead of raw kbps to avoid churn;
- hero state can show "Wi-Fi connecté mais limité" when Android validates Internet but reports very constrained capacity.

## Safety invariants unchanged
- no mobile routing ownership
- no automatic mobile probe
- no speedtest
- no framework connectivity reporting
- active recovery remains AUTOMATIC + Wi-Fi only


## 0.7.0-dev tranche — quality-aware efficiency

Field baseline remains immutable:
- 0.6.0-rc3 / versionCode 7
- bidirectional handoff field gate PASS
- canonical Drive installer unchanged

Development line:
- 0.7.0-dev / versionCode 8
- passive-quality hysteresis: 3 consecutive low-quality Wi-Fi observations before metrics refresh
- remaining Wi-Fi probe endpoints abort immediately if Android handoff starts
- telemetry schema v3 persists low-noise quality tiers, with in-place v2 migration
- manual optimization on metered Wi-Fi performs metrics-only refresh and zero HTTP micro-probes
- all mobile-routing and hidden-probe safety invariants remain unchanged


## Evidence-driven recovery loop
KINLINK 0.7.0-dev now evaluates whether automatic metric refreshes appear to improve passive Wi-Fi quality after two subsequent validated observations.
Results are retained as IMPROVED / UNCHANGED / DEGRADED / INCONCLUSIVE.
Two recent UNCHANGED/DEGRADED outcomes within the bounded window suppress further automatic recovery attempts.
This prevents repeated no-benefit actions.


## 0.7.0-dev diagnostic-depth tranche

Integrated:
- passive likely-cause classification with confidence;
- Android DNS server-count/private-DNS observation, no DNS override;
- passive outage duration evidence (MICRO / SHORT / LONG);
- cause-aware non-invasive guidance;
- cause-aware foreground notification;
- bounded cause-transition history;
- qualitative session health (HEALTHY / WATCH / DEGRADED / CRITICAL);
- once-per-version zero-network runtime self-test for DB schema, RecoveryMode readability and first NetworkCallback evidence.

Proven CI PASS already covers passive-cause diagnosis, guidance/notification and session-health commits.
Post-update runtime self-test is the current CI gate.
