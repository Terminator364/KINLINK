# NEXT_UNCOMMITTED_ACTION

## Current phone
- Installed: KINLINK 0.7.0 / versionCode 8.
- It may remain in use.
- Its canonical promotion was invalidated by post-install counter-audit.

## Consolidated successor — Stage A PASS
- Version: 0.7.1
- versionCode: 9
- Exact source/audit head: `9a6eac0c3f4e97d1b6685bda92fc5d04859ec2b5`
- Android CI run: `35590793691` — PASS
- design-lint run: `35590793645` — PASS
- Artifact ID: `10634896108`
- Artifact ZIP SHA-256: `96b903ba823196ef119bb77975b8bc6ad307b96206492960a7bd1c344c93d315`
- Unsigned APK SHA-256: `25ff61529a7bc009543fe39edad05bd356b4ec4664be9da591af45721c34feff`
- Signed APK SHA-256: `db2f73277eff1fa3da1a1082a7f71ecbef7ef79c8054e246cebfcc1414b66329`
- Signed size: 336843 bytes
- Canonical signer SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- APK Signature Scheme v3: PASS
- Signers: 1
- Stage A: PASS
- Stage B: PENDING target-phone evidence

## Counter-audit closed machine-side
CA-001 through CA-013 are integrated and machine-tested.

Key latest additions:
- exact version-scoped qualification SQL;
- current verdict cannot be forced by historical QUALIFIED/BLOCKED receipts;
- automatic HTTP outer wall-clock deadline;
- stale default-network callbacks fenced before/after reduction;
- stale callbacks cannot suppress delayed offline settling;
- stale manual Wi-Fi work aborts after handoff;
- DNS/HTTP helper threads are globally bounded;
- structured privacy-safe diagnostic ZIP core bundle;
- exhaustive P0 transport/resource scenario sweep;
- notification visibility request without blocking service;
- qualification receipts protected against pruning.

## Immediate internal action — no human gate yet
1. stage the exact signed 0.7.1 APK into Drive `KINLINK/FIELD_CANDIDATE`;
2. read it back from Drive and require SHA-256 `db2f73277eff1fa3da1a1082a7f71ecbef7ef79c8054e246cebfcc1414b66329`;
3. verify FIELD_CANDIDATE contains the current successor cleanly and remove obsolete delivery accumulation;
4. update Drive master/receipt;
5. only then ask for ONE in-place 0.7.1 install.

Current blocker is an internal file-handoff/session bridge for Drive upload, not a user action and not a reason to request another APK/install cycle.

## After one 0.7.1 install
Collect versionCode 9 evidence:
- core self-test;
- observer callback self-test;
- Wi-Fi -> validated cellular;
- cellular -> Wi-Fi return;
- resource qualification >=30 minutes.

Canonical INSTALLER replacement remains forbidden until Stage B PASS.

## Communication protocol
- Gmail start first.
- Work coherent 25-minute tranche.
- Intermediate feedback is queued/integrated without breaking tranche.
- Stop early only for true human gate.
- Gmail complete end report first and require returned Gmail message ID.
- Only then app message: check Gmail + Kinshasa date/time.
- Gmail label: KINLINK.
- APK never by Gmail.

## Resume command
`KINLINKGO`
