# NEXT_UNCOMMITTED_ACTION

## Proven release checkpoint

KINLINK 0.6.0-rc3 is:
- full-CI PASS;
- Android-lint PASS;
- network/permission fence PASS;
- stable-signed with the persistent KINLINK signer;
- canonical Drive promotion PASS;
- byte-for-byte Drive readback PASS.

Canonical installer:
- Drive file ID: `1EUEEkAFoX0pQEdCU3prP5UoawbzHoh4L`
- Name: `KINLINK_LATEST.apk`
- SHA-256: `4fb4ac36765b6d3728194bf7884f3c98afff625f4a5bc0637b825db0703a700c`

## Next true gate — human field validation

One in-place update only.

After install, validate:
1. app opens and shows KINLINK 0.6.0-rc3;
2. Wi-Fi observation/recovery remains healthy;
3. safe mode toggle is visible and immediate;
4. leave Wi-Fi coverage and confirm mobile data works normally;
5. return to Wi-Fi and confirm handoff back;
6. inspect local action/handoff receipts if any anomaly appears.

Do not create another installer before this field gate produces evidence, unless a machine-only defect is discovered first.
