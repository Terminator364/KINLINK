# KINLINK 0.7.0 — Signed Field Candidate Evidence

## Purpose

This is the **single consolidated field candidate** used to collect the two remaining field gates. It is **not** a canonical promotion and must not replace the RC3 Drive installer until all promotion gates pass.

## Source and CI

- Source commit: `9a4a0c5c1dd9e88eb2dd03983281b8115159a6ab`
- Android candidate CI run: `35549053623` — PASS
- design-lint run: `35549053626` — PASS
- CI artifact id: `10617413284`
- CI artifact name: `KINLINK-0.7.0-ci-build`
- Artifact ZIP SHA-256: `350492836325bb9b2644e90737f26bb63288e0b3aaecf24823e039570ce12209`

## Unsigned CI candidate

- Package: `com.terminator364.kinlink`
- versionCode: `8`
- versionName: `0.7.0`
- Variant: `candidate-unsigned`
- Unsigned APK SHA-256: `987fd96554b577cc8d03b0ac2b0715c25e755c2b8afe7be17a23e3177f2cc588`
- Public CI unsigned-state fence: PASS

## Stable-signed field candidate

- File name: `KINLINK-0.7.0-field-candidate-signed.apk`
- Signed APK SHA-256: `aed0643ea4d03e46f9befdc99866bcbbb4a599c9fddbb552ea7397e285ea8b79`
- Size: `316363` bytes
- Signer certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- APK Signature Scheme v3: verified
- Signers: 1
- Signing tool fix commit: `fcc13cf5de055ba62f20cfc92944d63573a72175`

The first signing attempt was intentionally rejected by the local verification wrapper because the parser expected an older `apksigner` label. The APK certificate itself was already the canonical certificate. The parser was corrected, the rejected output was deleted, and the same hash-pinned unsigned CI APK was signed again and verified successfully.

## Binary-content comparison

All 71 original APK ZIP entries have identical CRC and uncompressed size before and after signing. The signed APK adds only the expected `META-INF` signature entries plus the APK signing block managed by `apksigner`.

## Stage A verdict

- MACHINE: PASS
- MIGRATION: PASS
- SIGNER_CONTINUITY: PASS

**Stage A: PASS — eligible as one signed field candidate.**

## Canonical promotion is still forbidden

The following gates remain required on this exact field candidate:

- FIELD_HANDOFF
- RESOURCE_QUALIFICATION (>=30 minute target-device evidence)

The canonical RC3 installer remains unchanged until all five gates PASS.
