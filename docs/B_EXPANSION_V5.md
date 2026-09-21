# KINLINK — B EXPANSION V5 (B91-B100)

Status: canonical W2 research-depth layer. Macro denominator remains 80.

This layer turns Mobile Vault / Carrier Wallet from a vague budget feature into a permission-aware, multi-SIM-safe, offline-first accounting architecture.

## B91 — Usage Access is optional capability, never core dependency
**Parent macros:** K017, K041, K060  
**Stage:** DESIGN_ONLY

Treat PACKAGE_USAGE_STATS / Settings Usage Access as an explicit optional lane. KINLINK core must remain correct when the user never grants it or later revokes it.

**Acceptance:** Capability discovery distinguishes granted/revoked; core observer/Mobile Vault user-entered mode remains functional with no Usage Access.

**Primary source:** https://developer.android.com/reference/android/app/usage/NetworkStatsManager

## B92 — NetworkStats query latency budget
**Parent macros:** K017, K058  
**Stage:** DESIGN_ONLY

NetworkStatsManager queries may take seconds, so every query runs on one bounded worker with timeout/cancellation and never on the UI or ConnectivityManager callback thread.

**Acceptance:** Instrumentation proves no NetworkStats query executes on main/connectivity callbacks and a stalled query cannot block cockpit or recovery.

**Primary source:** https://developer.android.com/reference/android/app/usage/NetworkStatsManager

## B93 — Aggregate-mobile fallback is not per-SIM truth
**Parent macros:** K016, K017, K060  
**Stage:** DESIGN_ONLY

When restricted subscriber identifiers are unavailable, null-subscriber NetworkStats results may aggregate all mobile networks. Label this DEVICE_MOBILE_AGGREGATE and never attribute it to one SIM/plan.

**Acceptance:** Two-SIM fixture never maps aggregate bytes to a specific plan; per-plan zone stays UNKNOWN until attributable evidence exists.

**Primary source:** https://developer.android.com/reference/android/app/usage/NetworkStatsManager

## B94 — Subscription ID privacy boundary
**Parent macros:** K016, K043  
**Stage:** DESIGN_ONLY

If per-subscription behavior is truly needed, use Android Subscription ID under explicit READ_PHONE_STATE capability and keep it local. Never use/export ICCID, IMSI, IMEI or other non-resettable identifiers.

**Acceptance:** No privileged/non-resettable telephony identifier API is present; diagnostics redact subscription identity; denial degrades to aggregate/unknown mode.

**Primary source:** https://developer.android.com/identity/user-data-ids

## B95 — Active-data subscription is not always default-data subscription
**Parent macros:** K016, K066  
**Stage:** DESIGN_ONLY

Model the actually active data subscription separately from default-data preference because opportunistic networks can differ. Telephony observations pinned to a subId must not be silently reused after active-data changes.

**Acceptance:** Default/active-data switch tests invalidate stale per-sub evidence and do not merge plans.

**Primary source:** https://developer.android.com/reference/android/telephony/TelephonyManager

## B96 — NetworkStats usage callbacks are advisory while process lives
**Parent macros:** K017, K045, K058  
**Stage:** DESIGN_ONLY

registerUsageCallback is an efficiency hint while the process is alive, not a durable wakeup/alarm guarantee. Mobile Vault correctness must derive from persisted plan state plus fresh reconciliation.

**Acceptance:** Process-kill/restart test does not assume missed callback means zero usage; fresh reconciliation runs before spending decisions.

**Primary source:** https://developer.android.com/reference/android/app/usage/NetworkStatsManager

## B97 — Plan-cycle wall time versus control-loop monotonic time
**Parent macros:** K015, K029, K031  
**Stage:** DESIGN_ONLY

Use wall-clock/calendar semantics only for carrier plan cycle/expiry, while cooldowns, probe deadlines, evidence windows and relapse timing remain on elapsedRealtime/monotonic time.

**Acceptance:** Manual clock/timezone change cannot shorten cooldowns or fake causal windows; explicit plan expiry follows intended calendar semantics.

**Primary source:** https://developer.android.com/reference/android/os/SystemClock

## B98 — Carrier Wallet evidence provenance and confidence
**Parent macros:** K015, K017, K032  
**Stage:** DESIGN_ONLY

Every balance/usage observation stores value, semantic kind, source, observed-at time, attribution scope, confidence and adapter/version provenance. Conflicting sources remain CONFLICT instead of being averaged.

**Acceptance:** Reconciliation tests preserve disagreement and choose HOLD/UNKNOWN when attribution or freshness is insufficient.

**Primary source:** internal:MOBILE_VAULT_AND_CARRIER_WALLET

## B99 — Counter-reset and reboot reconciliation for plan usage
**Parent macros:** K015, K017, K046  
**Stage:** DESIGN_ONLY

A lower Android traffic counter after reboot/reset closes the old counter segment; it must never subtract usage or create free quota. Reconcile with persisted segments and higher-authority plan evidence.

**Acceptance:** Counter rollback test never decreases proven cycle usage and never promotes a stricter zone back to NORMAL solely because the device counter reset.

**Primary source:** internal:field-budget-hardening

## B100 — User-entered plan/expiry is first-class offline fallback
**Parent macros:** K015, K041, K060  
**Stage:** PARTIAL

Because privileged carrier/account data may be unavailable, allow a simple local user-entered plan size, expiry and reserve policy to drive Mobile Vault safely without cloud, carrier login or privileged IDs.

**Acceptance:** A device with no Usage Access/READ_PHONE_STATE/carrier adapter can configure plan envelopes locally in MB/GB and receives truthful UNKNOWN where usage cannot be attributed.

**Primary source:** internal:A+C-zero-dollar-offline-first


## W2 consequence

The executable `MobilePlanVaultPolicy` introduced on the 0.8 branch is deliberately source-agnostic. It can enforce envelopes only after usage evidence is attributed. B91-B100 define how future Usage Access, NetworkStats, telephony subscription context, user-entered plan data and carrier adapters can feed it without inventing per-SIM certainty.

## Research facts integrated

- Android NetworkStats device/user-wide access is optional via Usage Access and may be slow.
- `registerUsageCallback` callbacks continue while the process lives; they are not a durable wakeup substitute.
- Restricted subscriber identifiers can force aggregate mobile statistics.
- Android recommends Subscription ID rather than non-resettable telephony IDs when SIM association is genuinely needed.
- active-data and default-data subscription semantics can differ.
