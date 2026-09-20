# Test and Release Gates

A successful Gradle build is not a release gate.

## Mandatory pre-field scenarios
- Wi-Fi healthy
- cellular healthy
- cellular -> Wi-Fi
- Wi-Fi -> cellular
- trusted Wi-Fi without WAN
- healthy LAN without Internet
- captive portal
- DNS failure
- IPv6 broken / IPv4 healthy
- data stall
- network flapping
- simulated mobile bundle exhausted
- mobile budget reached
- control-process crash
- network-process crash
- reboot
- Android battery saver
- thermal throttling condition
- update from previous signed version
- diagnostic export after crash
- fail-open recovery

## Evidence
Every scenario produces a machine-readable receipt containing:
- build SHA
- device/API profile
- scenario
- expected result
- actual result
- pass/fail/inconclusive
- logs/telemetry references
