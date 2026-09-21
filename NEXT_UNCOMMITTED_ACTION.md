# NEXT_UNCOMMITTED_ACTION

## Stable canonical baseline
0.6.0-rc3 / versionCode 7 remains installed and Drive-canonical. Do not uninstall it merely to force an update.

## Final consolidated field candidate
- Version: 0.7.0
- versionCode: 8
- Source commit: `4153720cde8c6bd0004b8e69baeadf5254735abe`
- Android candidate CI run: `35550539216` — PASS
- design-lint run: `35550539365` — PASS
- Final signed APK SHA-256: `cea3468340a8f81dc38cc1ba09abb68e8f9ccc1634e223f275b0e92b083ef0bc`
- Signer certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- APK Signature Scheme v3: PASS
- Signers: 1
- Stage A: PASS
- Canonical promotion: FORBIDDEN until Stage B

## Important
The earlier signed artifact `aed0643...` was superseded before delivery and must never be installed.

## Next human gate
1. Install ONLY the exact final signed 0.7.0 candidate over RC3.
2. Do not uninstall RC3 first. If Android rejects the in-place update, stop and preserve RC3.
3. Open KINLINK once after update.
4. Keep the phone off the charger for a clean resource window; if it was charging, unplugging automatically resets a fresh 30-minute baseline.
5. Produce one Wi-Fi -> validated cellular handoff and one cellular -> Wi-Fi return.
6. KINLINK version-scoped receipts decide QUALIFIED/BLOCKED automatically; old RC3 evidence cannot qualify v8.
7. Export diagnostic only when promotion evidence must be transferred.
8. Do not replace Drive canonical RC3 until FIELD_HANDOFF + RESOURCE_QUALIFICATION PASS on this exact hash.

## Resume command
`KINLINKGO`
