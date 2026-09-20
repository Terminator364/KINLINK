# KINLINK build status

## Promoted integrated candidate

- Version: **0.5.0-rc2**
- Source commit: `7124e1d1cc9b3430f24af74fc2501371c182c25a`
- CI run: `35533990740` — **PASS**
- Unit/regression tests: **PASS**
- Debug build gate: **PASS**
- Stable signing: **PASS**
- Signer SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Final APK SHA-256: `5f5229c0d17301dac46032318c0ef9bec344e2c2e9848ccd7378d1f3311b8d6a`
- Drive replacement: **PASS**
- Drive readback: **BYTE-IDENTICAL**
- Field verification on target phone: **PENDING**

RC2 materially exceeds RC1: automatic bounded Wi-Fi recovery, no automatic negative Android hints, metered-Wi-Fi guard, cooldown/rate limits, battery/thermal guard, reboot/update restart and durable action receipts.

The optional VpnService/TUN stabilizer remains gated and is not claimed as production-ready.
