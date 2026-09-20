# KINLINK Resource Qualification Protocol

## Purpose

Resource claims must be evidence-backed. KINLINK must not be promoted on statements such as "lightweight" or "battery friendly" without bounded measurements.

## Current qualification limits

These are the current machine/field gates from RecoveryQualificationPolicy:

- process RAM delta: <= 24 MiB
- battery drain estimate: <= 1.5 percentage points/hour
- added latency attributable to the recovery layer: <= 10 ms

These limits are qualification gates, not guarantees for every device or radio condition.

## Runtime evidence collection

0.7.0-dev records lightweight service-session receipts:

- elapsed session duration
- process PSS at session start
- process PSS at session end
- PSS delta
- battery percentage start/end when Android exposes it
- battery percentage/hour only when the session lasts at least 30 minutes

Short sessions are explicitly INCONCLUSIVE for hourly battery rate.

## Battery attribution caveat

Android battery percentage is device-wide.
A battery percentage drop during a KINLINK session is not proof that KINLINK caused the entire drop.

Therefore:
- use multiple sessions;
- compare similar device conditions where possible;
- never treat one session as proof;
- use the evidence as a release guard, not a laboratory-grade energy model.

## RAM interpretation

Process PSS is sampled only at session boundaries.
This keeps measurement overhead very low.

A positive PSS delta is not automatically a leak.
Repeated upward drift across comparable long sessions is the relevant warning signal.

## Background churn gates

The current design reduces churn by:
- deduplicating NetworkTruth callbacks by semantic fingerprint;
- storing quality tier rather than raw bandwidth fluctuations;
- deduplicating unchanged foreground-notification text;
- caching device-wide mobile TrafficStats samples for 15 seconds;
- using no polling loop for connectivity or mobile counters.

## Release gate

Before the next consolidated signed candidate:
1. machine CI must pass;
2. at least one >=30 minute target-device session must produce a runtime budget receipt;
3. no repeated unexplained PSS growth may be present;
4. no evidence may exceed the configured resource limits without an explicit BLOCKED verdict;
5. strong VPN/TUN recovery remains disabled regardless of ordinary resource results until its separate qualification completes.
