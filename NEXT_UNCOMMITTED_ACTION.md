# NEXT_UNCOMMITTED_ACTION

## Proven machine checkpoint

KINLINK 0.5.0-rc2 passed CI and stable-sign promotion.

- source: `7124e1d1cc9b3430f24af74fc2501371c182c25a`
- CI: `35533990740` PASS
- signed APK SHA-256: `5f5229c0d17301dac46032318c0ef9bec344e2c2e9848ccd7378d1f3311b8d6a`
- signer: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Drive readback: byte-identical

## Next human gate

Install the existing `KINLINK_LATEST.apk` as an **in-place update**. No uninstall should be required because the signer certificate is continuous and versionCode increased from 5 to 6.

Check:
1. app opens as 0.5.0-rc2;
2. healthy Wi-Fi never shows the previous contradictory negative message;
3. Autopilot notification/service is active;
4. normal browsing remains unaffected.

## Next machine work after field gate

Continue M5 prototype qualification (DNS resilience + watchdog + resource/latency benchmark) without promoting it until evidence gates pass.
