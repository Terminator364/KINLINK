# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

KINLINK M2 is CI-verified, persistently signed, promoted to Drive, and read back byte-for-byte.

- Source commit: `6010398a7f64e62d301c3e5b4317c6ce5f927b48`
- CI run: `35527033841` — PASS
- Stable APK SHA-256: `ca09e40d0ad5d8e3bec8ddfe56e80bdc6a2449a653ace75f7877385e697d12a7`
- Stable signer SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Drive: `KINLINK/INSTALLER/KINLINK_LATEST.apk`
- Private signing material: `KINLINK/SIGNING`
- Drive readback: byte-identical; installer folder contains one APK only.

## Next action — HUMAN GATE

Because the M1 currently installed on the phone used a disposable CI debug certificate, Android cannot
upgrade it in place to the new stable certificate.

Perform this one-time migration:
1. uninstall the currently installed KINLINK;
2. install `KINLINK_LATEST.apk` from `KINLINK/INSTALLER`;
3. open KINLINK with Wi-Fi active;
4. press **Optimiser le Wi-Fi maintenant** once.

Expected behavior:
- no forced switch to mobile data;
- bounded Wi-Fi-only probe;
- Android connectivity re-evaluation;
- refreshed network metrics request;
- clear success/degraded explanation in the cockpit.

After this migration, do not rotate the stable signing key. Future KINLINK APKs should update in place.
