# NEXT_UNCOMMITTED_ACTION

## Stable field baseline
0.6.0-rc3 / versionCode 7 remains installed and Drive-canonical.

## Frozen candidate line
0.7.0 / versionCode 8.

## Current final app head for this tranche
`6b5c31d7c16d916c0d5768fd0284823165d3c210`.

No more app-code changes should be made until this exact head finishes candidate CI.

## Latest earlier full machine PASS
`9a4a0c5c1dd9e88eb2dd03983281b8115159a6ab`.

A signed artifact from that earlier head was produced and verified but was **never delivered or installed**. It is superseded before field delivery because additional qualification hardening was added.

## Hardening added after that preliminary signing
1. executable SQLite migration simulation v1 -> v5 with sentinel-data preservation;
2. successful self-test receipts are counted separately from failures;
3. field-candidate qualification is automatically evaluated and sealed locally;
4. every qualification receipt is scoped to the running versionCode;
5. RC3 handoff/self-test history therefore cannot qualify 0.7.0;
6. resource PASS/BLOCKED evidence is version-scoped;
7. foreground notification surfaces QUALIFIED/BLOCKED state;
8. main UI shows 0.7.0 field qualification state;
9. diagnostic export computes the field verdict from current-version evidence only;
10. intermediate CI runs are automatically cancelled so only the newest branch head matters.

## Current machine gate
- head: `6b5c31d7c16d916c0d5768fd0284823165d3c210`
- design-lint: pending/running at checkpoint time
- Android candidate CI: pending at checkpoint time
- canonical RC3 untouched

## Next durable action
1. read both CI results for `6b5c31d7c16d916c0d5768fd0284823165d3c210`;
2. if both PASS, download that exact unsigned release-like artifact;
3. verify ZIP digest + APK hash + manifest identity;
4. stable-sign exactly that artifact with the canonical KINLINK signer;
5. verify signed hash, v3 signature and certificate fingerprint;
6. deliver/install only that one consolidated field candidate;
7. collect automatically version-scoped handoff + >=30 min resource evidence;
8. canonical promotion remains forbidden until Stage B PASS.
