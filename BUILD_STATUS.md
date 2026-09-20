# KINLINK build status

## Integrated candidate — 0.4.0-rc1

Status: **CI PASS / stable-signed / Drive-promoted / field verification pending**

Evidence:
- Source commit: `a818ddf31bf952a52a40d3df02cddb1e7914b933`
- GitHub Actions run: `35529443848` — PASS
- Unit and regression tests: PASS
- Debug APK build: PASS
- Stable APK SHA-256: `030015ccd39f2536dea071434c987a243ecc4d0e29067d7b8b3e0ce41bd0e8d5`
- Stable signer certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Stable APK size: 6,502,991 bytes
- Drive readback: byte-identical
- Drive installer directory: one APK only, `KINLINK_LATEST.apk`

## What RC1 integrates

- M3 Resilience Evidence Engine
  - Android VALIDATED wins over isolated probe failure
  - exact HTTP 204 acceptance
  - bounded fallback endpoint
  - captive-portal priority
  - field contradiction regression tests
- M4 Mobile Vault / Cost Guard
  - passive TrafficStats mobile byte deltas
  - reboot/reset-safe baseline
  - optional daily MiB envelope
  - LOW at 80%, EXHAUSTED at 100%
  - KINLINK mobile recovery hold at LOW/EXHAUSTED
- Safe adaptive layer
  - bounded 15-minute stability window
  - instability score / flapping flag
  - no-regret policy
  - healthy validated Wi-Fi is left alone
  - no hidden mobile probes
- Telemetry hardening
  - one canonical background writer
  - no UI/service duplicate ledger append

## Current gate

The candidate is not labeled final 1.0 yet because the target phone has not field-verified this integrated build and the optional M5 VpnService/TUN stabilizer is intentionally not active.

The next valid transition is an **in-place phone update** followed by one focused Wi-Fi resilience check and optional Mobile Vault budget setup. M5 may advance only after this RC1 field gate.
