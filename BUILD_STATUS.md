# KINLINK build status

## Installed phone
- KINLINK 0.7.2 / versionCode 10
- installed and usable

## Frozen 0.7.3 field candidate
- versionName 0.7.3-dev / versionCode 11
- source `2aeac4cf801e60986c33347676e12310790d1d98`
- design-lint `35626585191`: PASS
- Android CI `35626585156`: PASS
- responsive matrix: 36/36 assertion PASS + manual visual PASS
- signed SHA-256 `d268542a4e6d4a4a5869deddd71bf0aec32c46386d2193cf96da6b825e68877d`
- signer `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`

## Delivery-security repair
Observed incident: old canonical rollback 0.6.0-rc3/versionCode7 was still exposed as `INSTALLER/KINLINK_LATEST.apk` while versionCode10 was installed. APK integrity/signature were valid, but Android rejected the downgrade with a generic invalid-package message.

Repair:
- old INSTALLER renamed `ROLLBACK_CANONICAL_DO_NOT_INSTALL`;
- new human `INSTALLER` contains exactly one `KINLINK_INSTALL_NOW.apk`;
- Drive file ID `1dj2j2zhLmUPb63NVIaMx0jElpNdb04ky`;
- readback SHA-256 exact match PASS;
- signer PASS;
- monotonic version gate 11 > 10 PASS.

## Current gate
One in-place install of `KINLINK/INSTALLER/KINLINK_INSTALL_NOW.apk`. Do not uninstall 0.7.2 first. Canonical promotion remains blocked until target-device field verification passes.


## 0.8 integrated successor — ACTIVE

0.7.3/v11 is installed but field-counter-audit rejected its near-final claim.

0.8.0-dev/v12 branch:
`dev/0.8.0-integrated-truth-and-control`

This is a coherent successor line, not a micro-beta. No user install until its entire integrated batch passes fresh CI, rendered visual/truthfulness audit, signing and Drive readback.

Main corrections under qualification:
- no user-facing passive 100/100 pseudo-quality;
- user-reported bad experience and unstable history override optimistic framework estimates;
- MB/GB mobile data UI;
- compact profile/data controls;
- causal-benefit wording fence;
- repeated ineffective Mobile Assist escalation rather than repeated metric-refresh theatre.
