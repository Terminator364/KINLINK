# Android Runtime Design

## SDK baseline
Initial target for first field beta:
- compileSdk: 37 (Android 17)
- targetSdk: 37
- minSdk: 30 (Android 11)
- JDK: 17

Rationale: the user's current phone is modern, while API 30 gives a clean baseline for ConnectivityDiagnosticsManager. Broader support can be added later if evidence justifies the added complexity.

## Persistence modes
KINLINK has two runtime modes.

### Lite Observer
Used before VPN consent or when the stabilizer is disabled.
- no packet interception
- app/worker gathers snapshots when alive
- useful for diagnostics, but not guaranteed continuous background autonomic operation

### Full Autopilot
User grants VPN consent once and may enable Always-on VPN.
- VpnService provides a system-supported long-lived component
- aggressive optimizations remain feature-gated; an always-on service does not mean constant probing
- VPN lockdown / "block connections without VPN" remains OFF by default
- data plane must have fail-open teardown

The first beta must field-test the battery and reliability cost of an always-on minimal data plane before making it the default recommendation.

## Foreground-service constraints
Do not depend on starting an arbitrary foreground service from the background. Android restricts background FGS launches. Prefer platform-supported VPN lifecycle for Full Autopilot and WorkManager for deferrable jobs.

## Local-network permission readiness
For targetSdk 37, explicitly model ACCESS_LOCAL_NETWORK as a capability gate for LAN diagnostics and local project integrations.
