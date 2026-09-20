# KINLINK field evidence ledger

## 2026-09-20 — contradiction from earlier build

Observed:
- WIFI_HEALTHY
- LAN available
- Android-derived Internet healthy
- simultaneous negative Wi-Fi revalidation message

Root cause:
A failed external connectivity endpoint had excessive authority.

Permanent fix:
Android VALIDATED cannot be downgraded by external probe failure. RC2 also forbids automatic negative connectivity hints entirely.

## 2026-09-20 — RC2 machine promotion

- version: 0.5.0-rc2
- source: 7124e1d1cc9b3430f24af74fc2501371c182c25a
- CI run: 35533990740 PASS
- final SHA-256: 5f5229c0d17301dac46032318c0ef9bec344e2c2e9848ccd7378d1f3311b8d6a
- signer SHA-256: 2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3
- Drive readback: byte-identical

Field installation/behavior remains pending.
