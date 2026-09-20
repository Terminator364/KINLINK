# Test and Release Gates

## RC2 machine gate — PASS
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
- recovery cooldown/hourly cap
- reboot and package update restart
- diagnostic export

## Required assertions
- no automatic cellular probe
- no automatic metered-Wi-Fi probe
- no automatic negative Android hint
- Android VALIDATED cannot be downgraded by endpoint failure
- stable Wi-Fi is left alone
- recovery is rate-limited and receipt-carrying
- resource pressure suspends recovery
- networking remains fail-open

## Remaining RC2 gate
Target-phone in-place update and field behavior.
