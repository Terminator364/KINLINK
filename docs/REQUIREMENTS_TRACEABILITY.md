# KINLINK Requirements Traceability — current engineering truth

This file prevents the large product specification from being mistaken for an already-complete implementation. It separates **release-blocking user-level requirements**, **implemented/qualified features**, and **later gated architecture**.

Status meanings:
- **PROVEN** — implemented and protected by code/tests/CI evidence.
- **FIELD_PENDING** — machine-proven but exact signed successor still needs target-phone evidence.
- **PARTIAL** — useful subset exists, but the broader specification is not complete.
- **GATED_FUTURE** — deliberately not enabled in the current release because its own safety gate is not closed.

## P0 safety and resilience

| Requirement | Status | Current proof / note |
|---|---|---|
| No automatic cellular probe/speedtest | PROVEN | central Wi-Fi-only recovery gate + manifest/API/candidate-contract fences + scenario tests |
| No automatic metered-Wi-Fi HTTP probe | PROVEN | policy branch + execution-time metered abort + contract audit |
| Android VALIDATED outranks endpoint failure | PROVEN | recovery/optimizer policies never report negative connectivity to Android |
| Preserve LAN independently from WAN | PROVEN | LAN_OK_WAN_DOWN state + scenario matrix |
| No routing ownership / VpnService in current release | PROVEN | source/manifest CI fences |
| Observation-only user mode | PROVEN | persistent RecoveryMode + central ActiveRecoveryPolicy |
| Bounded automatic recovery | PROVEN | cooldown/hourly cap/circuit breaker + hard probe envelope + watchdog |
| Battery/thermal/low-memory recovery suppression | PROVEN | DeviceResourceGuard + policy tests |
| Privacy-safe telemetry | PROVEN | no payloads/secrets; new rows persist topology booleans, not raw gateway/interface |
| Stable signer continuity | PROVEN for release process | canonical cert is pinned; exact successor still must be signed with it |
| Offline/local-first operation | PROVEN | normal observation/recovery requires no cloud service |

## Current 0.7.1 consolidated successor

| Area | Status | Notes |
|---|---|---|
| CA-001 hard recovery deadline | PROVEN | socket envelope + outer per-endpoint wall-clock timeout/disconnect |
| CA-002 battery quantization | PROVEN | 1-point coarse quantization no longer becomes false app-fault BLOCK |
| CA-003 bounded resource retry | PROVEN | max 2 attempts; hard PSS/churn failures are not endlessly retried |
| CA-004 current field-status truthfulness | PROVEN | cockpit/notification use current authoritative assessment |
| CA-005 qualification receipt liveness | PROVEN | irreplaceable self-test/qualified receipts pinned against normal pruning |
| CA-006 version receipt isolation | PROVEN | exact SQL equality for version-scoped qualification evidence |
| CA-007 stale historical QUALIFIED | PROVEN | live verdict can be revoked by newer blocking evidence |
| CA-008 DNS/socket wall-clock proof | PROVEN | outer probe deadline + forced disconnect + tests |
| CA-009 stale default-network callback | PROVEN | callback network must still equal Android active default |
| CA-010 stale manual post-handoff refresh | PROVEN | manual optimizer rechecks captured Wi-Fi before any final refresh |
| CA-011 bounded diagnostic workers | PROVEN | HTTP/DNS helper work shares a max-2 fail-open executor; saturation rejects new work |
| Exact signed 0.7.1 target-phone field gate | FIELD_PENDING | no human install until final exact-head CI/audit/signing is closed |

## User-facing product functions

| Function | Status | Notes |
|---|---|---|
| Always-on observer / foreground service | PROVEN machine-side | boot/package-replace restart is receipt-carrying and fail-open |
| Main French cockpit | PROVEN machine-side | current state, transport, Internet, budget, reliability, guidance |
| Automatic Wi-Fi recovery | PROVEN machine-side | bounded/reversible; never owns cellular routing |
| Manual “Optimiser le Wi-Fi” | PROVEN machine-side | Wi-Fi only; metered Wi-Fi = zero HTTP |
| Mobile prudence threshold | PROVEN machine-side | Android device-wide TrafficStats heuristic; not operator balance |
| Incident marker | PROVEN machine-side | passive local snapshot, zero probe |
| Field qualification status | PROVEN machine-side | version-scoped receipts and current assessment |
| Diagnostic export | PARTIAL | 0.7.1 now exports a privacy-safe ZIP core bundle (`manifest.json`, `summary.json`, `recent_actions.jsonl`, `report.txt`); the larger canonical bundle still has additional future files to implement |
| Weekly report | PARTIAL | diagnostic contains “This week” summary; dedicated richer weekly report engine is not yet implemented |
| Drive install/distribution master | PROVEN operationally | INSTALLER / FIELD_CANDIDATE / SIGNING / master continuity structure |
| Gmail tranche reporting | PROVEN operationally | dedicated KINLINK label + mandatory 25-minute protocol |

## Larger architecture that must not be falsely claimed complete

| Architecture item | Status | Gate |
|---|---|---|
| VpnService/TUN strong stabilizer | GATED_FUTURE | M5 DNS/watchdog/RAM/battery/latency/rollback qualification |
| Independent network data-plane process | GATED_FUTURE | only required when strong stabilizer is enabled |
| Personal Network Twin / learner / shadow evaluation | GATED_FUTURE | evidence model and resource budget not yet qualified |
| Full carrier/operator wallet integration | GATED_FUTURE | current build only has local device-wide mobile-byte prudence |
| Full structured diagnostic ZIP schema | PARTIAL | current TXT export is useful but does not satisfy the complete ZIP layout |
| Autonomous cloud/BCP diagnostic bridge | GATED_FUTURE | local-first remains default; no paid cloud dependency |

## Release rule

A release can be called **functionally ready at the user's current level** only when:
1. all P0 rows remain PROVEN;
2. exact-head Android CI + design-lint pass;
3. candidate contract and counter-audit pass;
4. stable signer continuity is verified on the exact APK;
5. Drive readback/hash evidence matches;
6. target-phone handoff and resource gates pass;
7. no known blocking finding is open.

GATED_FUTURE items are not silently counted as completed current-release features.
