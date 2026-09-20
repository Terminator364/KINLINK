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


## RC3 signed candidate ready

- Final CI source head: `dc7555b784562a059e16107c574839a66dff89cd`
- Final CI run: `35536189018` — PASS
- Signed APK SHA-256: `4fb4ac36765b6d3728194bf7884f3c98afff625f4a5bc0637b825db0703a700c`
- Stable signer certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Drive canonical replacement: PENDING because the container-to-Drive bridge expired; old installer intentionally preserved.
