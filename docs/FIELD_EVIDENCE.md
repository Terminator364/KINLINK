# KINLINK field evidence ledger

## 2026-09-20 — earlier contradiction

Observed:
- WIFI_HEALTHY;
- LAN available;
- Android-derived Internet healthy;
- simultaneous negative Wi-Fi revalidation message.

Root cause:
A failed external connectivity endpoint had excessive authority.

Permanent direction:
Android VALIDATED cannot be downgraded by an isolated external probe failure. Framework connectivity-report influence was later removed entirely.

## 2026-09-20 — RC2 machine promotion (historical)

- version: 0.5.0-rc2
- source: 7124e1d1cc9b3430f24af74fc2501371c182c25a
- CI run: 35533990740 PASS
- final SHA-256: 5f5229c0d17301dac46032318c0ef9bec344e2c2e9848ccd7378d1f3311b8d6a
- signer SHA-256: 2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3

Superseded as field baseline by RC3.

## 2026-09-20 — P0 mobile-handoff incident

Field symptom:
After leaving Wi-Fi coverage in an earlier build, mobile data appeared unusable.

Causality:
UNPROVEN.

P0 response:
- remove all reportNetworkConnectivity calls;
- keep cellular strictly observation-only;
- add CI fence against routing/network ownership APIs;
- add 5 s post-handoff quiet window;
- add 5 s recovery watchdog;
- abort stale Wi-Fi work if active transport changes;
- add handoff receipts and outcome evidence;
- add persistent observation-only safe mode.

## 2026-09-20 — RC3 promotion

- version: 0.6.0-rc3
- versionCode: 7
- signed APK SHA-256: 4fb4ac36765b6d3728194bf7884f3c98afff625f4a5bc0637b825db0703a700c
- signer SHA-256: 2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3
- canonical Drive file ID preserved: 1EUEEkAFoX0pQEdCU3prP5UoawbzHoh4L
- Drive readback: exact SHA-256 + signature PASS

## 2026-09-20 — RC3 target-phone field gate

Observed on target phone:
- KINLINK 0.6.0-rc3 installed;
- Wi-Fi -> cellular: PASS;
- 4G/4G+ active;
- mobile Internet available;
- UI explicitly showed Android controls cellular and KINLINK observes only;
- cellular -> Wi-Fi: PASS;
- final Wi-Fi state: ready / Internet available;
- original mobile-block symptom: NOT REPRODUCED.

One transient offline UI frame was seen during return handoff and was subsequently addressed in 0.7.0-dev with delayed loss publication.

Verdict:
RC3 BIDIRECTIONAL_HANDOFF_FIELD_GATE = PASS.
