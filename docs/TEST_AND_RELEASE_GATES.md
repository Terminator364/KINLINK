# Test and Release Gates — RC3

## RC2 baseline machine gate — PASS
- unit/regression tests
- APK compilation
- deterministic CI artifact hash
- stable signer continuity
- signed APK verification
- Drive replacement and byte-for-byte readback

## Mandatory behavior scenarios
- healthy validated Wi-Fi
- validated Wi-Fi + failed external endpoint
- unvalidated Wi-Fi + positive micro-probe
- unvalidated Wi-Fi + negative micro-probes
- metered Wi-Fi/hotspot
- cellular
- LAN without WAN
- captive portal
- flapping
- mobile budget low/exhausted
- battery saver
- severe thermal pressure
- Android low-memory pressure
- recovery cooldown/hourly cap
- recovery hard deadline
- transport changes during recovery
- 5-second observation-only settling window after transport handoff
- reboot and package update restart
- restart-storm degradation to observation-only
- diagnostic export

## Required assertions
- no automatic cellular probe
- no automatic metered-Wi-Fi probe
- no Android connectivity-validation hint of any kind
- production-source static fence rejects network-binding/routing ownership APIs
- Wi-Fi→mobile handoff is passively receipt-carrying
- Android VALIDATED cannot be downgraded by endpoint failure
- stable Wi-Fi is left alone
- recovery is rate-limited and receipt-carrying
- resource pressure (battery saver / thermal / low-memory) suspends recovery
- networking remains fail-open
- stale Wi-Fi work cannot continue after Android changes active transport
- repeated service relaunches suspend active recovery instead of escalating

## Remaining RC2 gate
Target-phone in-place update and field behavior.


## RC3 additional gates
- persistent observation-only safe mode
- pure-policy proof: active recovery = AUTOMATIC + Wi-Fi only
- manifest safety fence rejects CHANGE_NETWORK_STATE and VpnService
- diagnostic export includes aggregate handoff outcomes and watchdog abort evidence
- versionCode monotonic upgrade to 7

- Android lint must pass
- manifest must not request CHANGE_NETWORK_STATE, CHANGE_WIFI_STATE, MODIFY_PHONE_STATE or WRITE_SETTINGS


## 0.7.0-dev consolidated signed-candidate gate

Promotion is blocked unless all five readiness gates are PASS in the durable release-readiness ledger:

- MACHINE
- MIGRATION
- SIGNER_CONTINUITY
- FIELD_HANDOFF
- RESOURCE_QUALIFICATION

The ledger is stored at:
`.project-memory/RELEASE_READINESS_0_7_0_DEV.json`

`tools/release/verify_release_readiness.py --audit` verifies ledger consistency during design-lint.

`tools/release/verify_release_readiness.py --require-ready` is the promotion fence: it must fail while any mandatory gate is PENDING or BLOCKED.

Resource qualification is not satisfied by a short session, by charging/inconclusive battery evidence, or by a raw callback count that ignores session duration.
