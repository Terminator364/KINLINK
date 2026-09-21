# KINLINK 0.7.0 — Counter-audit after field install

## Scope

This counter-audit was opened after the exact signed 0.7.0 field candidate was installed successfully. It does not claim a field failure. It re-checks the sealed source against the canonical safety and qualification contracts before canonical promotion.

Sealed candidate:
- source commit: `4153720cde8c6bd0004b8e69baeadf5254735abe`
- versionCode: 8
- signed APK SHA-256: `cea3468340a8f81dc38cc1ba09abb68e8f9ccc1634e223f275b0e92b083ef0bc`

## Findings

### CA-001 — hard 5 s recovery deadline is not actually hard

The automatic unvalidated-Wi-Fi recovery path calls `WifiDoctorProbe.run()` synchronously.

The probe has two HTTP endpoints. Each endpoint currently allows:
- connect timeout: 1600 ms
- read timeout: 1600 ms

A conservative upper bound is therefore approximately:

`2 endpoints × (1600 ms connect + 1600 ms read) = 6400 ms`

The recovery watchdog is checked only after the probe returns. Therefore it can prevent follow-up actions after the deadline, but it cannot stop the probe itself at 5 s.

Canonical contract: every automatic recovery action has a hard 5 s fail-open deadline.

**Verdict: BLOCKING for canonical promotion.**

Required repair:
- make the probe’s own worst-case bound comfortably lower than 5 s; and
- regression-test the relationship between probe bound and watchdog deadline.

### CA-002 — 30-minute battery gate is too sensitive to Android integer battery quantization

Runtime battery capacity is sampled as an integer percentage.

At the minimum qualifying window of 30 minutes:
- 0 percentage-point drop -> 0 %/h
- 1 percentage-point drop -> approximately 2 %/h

The current limit is 1.5 %/h.

Therefore one ordinary 1-point battery step during the first 30-minute window becomes a BLOCKED resource verdict, even though that measurement is device-wide and cannot isolate KINLINK’s own battery cost.

Canonical documentation already states that battery evidence is not a claim that all drain belongs to KINLINK.

**Verdict: BLOCKING for reliable release qualification, not proof of a runtime networking defect.**

Required repair:
- make the policy quantization-aware;
- add explicit tests for a 1-point drop over ~30 minutes;
- retain strict blocking for clearly excessive drain.

### CA-003 — resource qualification has no bounded automatic retry after a persisted non-PASS verdict

The one-shot checkpoint marks itself written whenever its receipts persist, regardless of PASS / INCONCLUSIVE / BLOCKED.

A retry is scheduled only when SQLite persistence itself fails.

Consequences:
- a coarse/noisy battery sample can strand field qualification;
- a recoverable INCONCLUSIVE result can require service restart or a charger disconnect to open a new window.

**Verdict: BLOCKING for low-burden field qualification.**

Required repair:
- allow a small, bounded number of automatic clean-window retries for battery-only/inconclusive resource evidence;
- never retry indefinitely;
- never retry hard PSS-growth or callback-churn failures automatically.

### CA-004 — UI can display historical BLOCKED after a newer resource PASS

The field policy correctly uses the latest timestamped resource verdict. However the main UI currently treats the existence of any historical `FIELD_CANDIDATE_BLOCKED_V8` receipt as “terrain BLOQUÉ” unless the terminal qualified receipt already exists.

A newer resource PASS can therefore make the live assessment PENDING while the UI still displays BLOQUÉ until every remaining gate completes.

**Verdict: truthfulness/UX defect; include in the same consolidated repair to avoid another install.**

## Positive revalidation

The same counter-audit re-confirmed:
- cellular remains observation-only;
- active recovery central gate is Wi-Fi-only;
- automatic metered Wi-Fi probes are blocked;
- probes are pinned to the captured Wi-Fi network;
- failed probes do not report negative connectivity to Android;
- new telemetry rows write NULL to legacy raw interface/gateway columns;
- diagnostics expose topology booleans rather than raw IP/gateway values;
- signing certificate continuity is intact;
- 0.7.0 can remain installed safely while replacement engineering is performed.

## Decision

0.7.0 remains usable as an installed field build, but its canonical promotion proof is invalidated by this counter-audit.

No micro-beta chain is allowed.

The next install, if required, must be one consolidated successor that closes CA-001 through CA-004, receives a new versionCode so old v8 qualification receipts cannot qualify it, passes full CI, and is signed with the same canonical certificate.
