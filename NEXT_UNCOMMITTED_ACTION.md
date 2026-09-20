# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

KINLINK 0.4.0-rc1 is an integrated candidate, not a diagnostic-only micro-release.

- Source commit: `a818ddf31bf952a52a40d3df02cddb1e7914b933`
- CI run: `35529443848` — PASS
- Stable APK SHA-256: `030015ccd39f2536dea071434c987a243ecc4d0e29067d7b8b3e0ce41bd0e8d5`
- Stable signer certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Drive: `KINLINK/INSTALLER/KINLINK_LATEST.apk`
- Drive file ID: `1EUEEkAFoX0pQEdCU3prP5UoawbzHoh4L`
- Drive readback: byte-identical
- Installer folder APK count: 1
- Update continuity: same stable signer as M2/M3; no uninstall is expected

## RC1 integrated scope

1. M2 field contradiction permanently regressed.
2. Multi-signal Wi-Fi evidence arbitration.
3. Exact HTTP 204 connectivity confirmation.
4. Bounded fallback probe.
5. Mobile Vault passive byte guard and optional daily budget.
6. LOW / EXHAUSTED recovery hold.
7. Reboot/counter-reset-safe baseline.
8. Anti-flapping stability score.
9. No-regret adaptive autopilot.
10. Duplicate UI/service telemetry writes removed.
11. Android 15/16 observer lifecycle hardening retained.
12. Stable private signing retained.

## Next action — HUMAN FIELD GATE

1. Update the currently installed KINLINK **in place** with `KINLINK_LATEST.apk`.
2. Keep Wi-Fi active.
3. Open KINLINK and press **Optimiser le Wi-Fi maintenant** once.
4. Expected correction: a network already validated by Android must no longer become “Wi-Fi répond mal” merely because one micro-probe endpoint failed.
5. Optionally open **Configurer la protection mobile** and set a daily guard in MiB.
6. Return one screenshot of the main cockpit plus technical details.

## After field PASS

Continue directly into M5 qualification work:
- VpnService/TUN prototype in non-promoted branch/candidate;
- fail-open watchdog and teardown;
- bounded DNS resilience benchmark;
- per-flow policy benchmark;
- RAM/battery/latency comparison against plain Android;
- rollback test.

Do not activate M5 on the user’s real traffic until these gates prove that it improves resilience without creating a worse failure mode.
