# Fail-Open and Recovery Contract

P0 objective: KINLINK must not become a single point of failure.

## Rules
- VPN lockdown is not enabled by default.
- Data-plane watchdog has a hard deadline.
- On data-plane health failure: freeze new policy actions, persist evidence, close TUN/VPN cleanly, let Android networking recover.
- Control-plane failure must not force teardown of a healthy data plane unless lease/heartbeat expires.
- Corrupt policy database => load last-known-good policy or safe defaults.
- Corrupt telemetry database => quarantine telemetry, do not block networking.
- Failed update self-test => disable new engine feature flags and remain on safe behavior.

## Required chaos tests
- kill control process
- kill network process
- repeated network flapping
- Wi-Fi without WAN
- DNS blackhole
- IPv6 broken / IPv4 healthy
- captive portal
- cellular registered but data exhausted
- low battery / thermal severe
