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


## 0.7.0-dev resource + longitudinal diagnostics tranche

Added:
- lightweight service-session PSS/battery qualification receipts;
- charging sessions treated as battery-INCONCLUSIVE;
- resource qualification protocol and release thresholds;
- unchanged foreground notification updates deduplicated;
- device-wide mobile TrafficStats sampling cached for 15 seconds;
- telemetry retention bounded by age and row count;
- telemetry schema v4 structured interruption duration;
- cumulative and longest interruption duration in diagnostics.

Machine status:
- full PASS through `e8280bb1c32a019f0e8f32d2f8bd44073261ed02`;
- schema-v4 head `5cce76cade7361e821a8eee36be8f0f1e5528412`: design-lint PASS + full Android CI PASS.

The installed RC3 baseline remains unchanged.


## 0.7.0-dev deep-diagnostics + fail-safe tranche

Full machine PASS at `3467ffdcf5da78cfd17e0e13f9b73b6b9cf0f317`.

Added since the previous checkpoint:
- rolling reliability and slow-but-validated Wi-Fi burden;
- one-tap passive incident marker plus focused context timeline;
- recovery control-path timing and evidence-driven circuit breaker;
- privacy-safe IP topology (IPv4/IPv6 address/default-route booleans);
- separate passive causes for addressing vs route vs DNS/WAN;
- telemetry schema v5; new rows no longer persist raw interface/gateway values;
- bounded manual Wi-Fi DNS-vs-HTTP diagnosis on the exact captured Wi-Fi network;
- remaining HTTP probe endpoints abort when that original Wi-Fi is no longer active;
- post-update self-test failure suspends active recovery while observation remains;
- one passive >=30-minute runtime resource checkpoint per service session;
- runtime verification of required telemetry schema columns.

Stable target-phone baseline remains 0.6.0-rc3 and is not overwritten.


## 0.7.0-dev radio + reliability tranche

Integrated after the previous deep-diagnostics checkpoint:
- passive Wi-Fi radio-quality model (UNKNOWN / WEAK / FAIR / GOOD);
- Android NOT_CONGESTED and NOT_SUSPENDED capability observation;
- distinct passive causes for weak radio, possible congestion and suspended network;
- Autopilot suppresses pointless metric refreshes for weak-radio/congestion/suspended cases;
- readable 24 h reliability summary in the main cockpit;
- passive quality trend (IMPROVING / STABLE / DEGRADING / INSUFFICIENT);
- Autopilot profile recommendation from recent reliability burden;
- slow-link episode burden now contributes to reliability severity even without outright outages.

Canonical 0.6.0-rc3 remains untouched.
Current code head: `00e4df0d3869daaab777d2b159a1c8228d9b2c7c`; CI is running at tranche checkpoint time.


## 0.7.0 final consolidated field candidate

Stage A is PASS.

- source commit: `4153720cde8c6bd0004b8e69baeadf5254735abe`
- Android candidate CI: run `35550539216` PASS
- design-lint: run `35550539365` PASS
- final signed APK SHA-256: `cea3468340a8f81dc38cc1ba09abb68e8f9ccc1634e223f275b0e92b083ef0bc`
- signer certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- v3 signature: PASS
- signers: 1
- version-scoped field evidence: enabled
- charging-safe resource qualification: enabled
- automatic field QUALIFIED/BLOCKED receipts: enabled

Canonical 0.6.0-rc3 remains unchanged. Stage B requires target-phone FIELD_HANDOFF + RESOURCE_QUALIFICATION on this exact 0.7.0 artifact.
