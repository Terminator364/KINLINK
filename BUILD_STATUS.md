# KINLINK build status

## RC3 consolidated candidate

- Version: **0.6.0-rc3**
- versionCode: **7**
- P0 mobile handoff hardening: integrated
- Active recovery gate: **AUTOMATIC + Wi-Fi only**
- Cellular: **observation-only**
- User safe mode: **persistent**
- 5 s post-handoff quiet window: integrated
- 5 s recovery watchdog: integrated
- stale-network abort: integrated
- restart-storm degradation: integrated
- battery / thermal / low-memory guards: integrated
- CI source fence against network ownership APIs: integrated
- manifest fence against CHANGE_NETWORK_STATE / VpnService: integrated
- diagnostics include handoff outcomes and watchdog aborts

Stable signing and Drive promotion remain blocked until the RC3 CI run is fully green.


## Lifecycle evidence added

RC3 now persists BOOT_COMPLETED / MY_PACKAGE_REPLACED service-start outcomes and imports them into the local action ledger on the next successful service creation.
