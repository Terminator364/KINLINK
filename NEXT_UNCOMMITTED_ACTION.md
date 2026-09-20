# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

- M2 stable signing baseline promoted and installed.
- Target-phone field evidence exposed one P0 contradiction: WIFI_HEALTHY versus a negative single-endpoint probe result.
- Root cause is understood and converted into a permanent regression obligation.

## Current candidate — M3 Resilience Evidence Engine

M3 must:
1. preserve Android VALIDATED against isolated probe failure;
2. use a bounded fallback micro-probe only on explicit Wi-Fi action;
3. prioritize captive-portal evidence;
4. send negative Android revalidation hints only when Android is not validated and bounded probes also fail;
5. keep mobile data untouched;
6. pass tests + APK build;
7. preserve the stable signing certificate and update in place.

## Next action

Run CI for M3. On PASS:
- obtain the exact artifact;
- sign it with the existing KINLINK_UPDATE_KEY_V1;
- verify signer SHA-256 remains 2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3;
- replace Drive/KINLINK/INSTALLER/KINLINK_LATEST.apk;
- perform byte-identical readback;
- then request one focused update-in-place field check.
