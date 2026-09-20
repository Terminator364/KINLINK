# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

KINLINK M2 is CI-verified and promoted as the single Drive installer.

- Binary source commit: `6010398a7f64e62d301c3e5b4317c6ce5f927b48`
- CI run: `35527033841` — PASS
- APK SHA-256: `7960160c137ff9776ba5ea694bdd961a7ad2a645ac2f3c7a757e46b4cf8b1cc9`
- Drive: `KINLINK/INSTALLER/KINLINK_LATEST.apk`
- Drive readback: one APK only.

## Next action — HUMAN GATE

Install/update `KINLINK_LATEST.apk` on the target Android phone, open KINLINK,
keep Wi-Fi active, then press **Optimiser le Wi-Fi maintenant** once.

Expected behavior:
1. no forced switch to mobile data;
2. bounded Wi-Fi-only probe;
3. Android connectivity re-evaluation;
4. refreshed network metrics request;
5. clear success/degraded explanation in the cockpit.

After that single field check, use the resulting observation to decide whether M2 can become the stable baseline
or whether M3 data-plane work is justified. Do not produce another diagnostic-only APK.
