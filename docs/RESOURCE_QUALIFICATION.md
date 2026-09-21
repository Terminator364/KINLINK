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
Sessions where Android reports charging are explicitly INCONCLUSIVE, even when battery percentage stays flat at 100%. Sessions where battery percentage increases are also INCONCLUSIVE rather than being reported as 0%/h.

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

Runtime callback churn is evaluated as a rate normalized to the actual session duration rather than as a raw lifetime count. The current gate is 120 callback events per 30 minutes, scaled linearly with session duration.

## Automatic qualification checkpoint

0.7.0-dev schedules one one-shot resource checkpoint at approximately 30 minutes after observer-service start. It does not run a repeating network timer and does not send traffic.

The checkpoint:
- samples process PSS and device battery percentage;
- evaluates callback churn accumulated during the same session;
- records RUNTIME_BUDGET_CHECKPOINT;
- records one of RUNTIME_RESOURCE_GATE_PASS, RUNTIME_RESOURCE_GATE_INCONCLUSIVE or RUNTIME_RESOURCE_GATE_BLOCKED.

If the service is shutting down after a qualifying session and the one-shot checkpoint did not run, shutdown performs the same bounded evaluation before closing the ledger. This prevents a quiet network with no callbacks from silently missing resource evidence.

## Release gate

Before the next consolidated signed candidate:
1. machine CI must pass;
2. at least one >=30 minute target-device session must produce a runtime budget receipt;
3. no repeated unexplained PSS growth may be present;
4. no evidence may exceed the configured resource limits without an explicit BLOCKED verdict;
5. strong VPN/TUN recovery remains disabled regardless of ordinary resource results until its separate qualification completes.


## Charging and automatic clean baseline

The target-device battery gate must never treat a plugged-in flat battery as proof of low drain.

0.7.0 therefore samples Android charging state at both resource-window endpoints. Battery-rate evidence is usable only when both endpoints are explicitly not charging.

If Android broadcasts ACTION_POWER_DISCONNECTED while the observer service is running, KINLINK automatically:
- resets the runtime resource baseline;
- resets callback-churn counting for the new resource window;
- cancels the obsolete delayed checkpoint;
- schedules one new bounded 30-minute checkpoint;
- records a local version-scoped baseline-reset receipt.

No network probe is launched by this reset.


## Resource verdict recovery

A historical BLOCKED receipt is not permanently sticky. The field-candidate gate compares the timestamps of the most recent version-scoped PASS and BLOCKED resource receipts:
- a newer BLOCKED remains blocking;
- a newer clean PASS supersedes an older BLOCKED;
- no PASS remains PENDING/INCONCLUSIVE.

This permits a controlled clean re-measurement after an adverse or abnormal session without erasing the earlier evidence.

## Receipt persistence fail-open

Qualification state is only marked persisted after SQLite confirms the receipt write. If a runtime resource receipt cannot be persisted, KINLINK does not claim that checkpoint as complete and schedules a bounded local retry. Self-test completion flags likewise advance only after their evidence receipt is stored.
