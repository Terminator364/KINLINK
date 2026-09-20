# KINLINK build status

## Current evidence

- M1 Observer Core: field-installed and operational.
- Passive background observer: implemented.
- Persistent bounded telemetry ledger: implemented.
- Explicit Wi-Fi Doctor: implemented.
- M2 active Wi-Fi optimizer: committed for CI verification.
- Android 15/16 foreground-service hardening: observer migrated away from time-limited dataSync to declared specialUse.
- Mobile-data probe: forbidden by policy.
- Android routing takeover / VPN data plane: not enabled.

## M2 gate

M2 must pass unit tests and debug-APK build before it can replace the installed M1.
The optimizer is bounded: Wi-Fi only, explicit user action, tiny connectivity probe,
Android connectivity re-evaluation hint, bandwidth-metric refresh, and fail-open behavior.
The observer service must also survive Android 15/16 foreground-service type rules.
