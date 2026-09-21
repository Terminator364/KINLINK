# KINLINK 0.7.0 — Final Signed Field Candidate Evidence

## Purpose

This document seals the **single consolidated 0.7.0 field candidate** used to collect the two remaining target-phone gates. It is **not** a canonical promotion. The installed/canonical 0.6.0-rc3 Drive installer remains unchanged until Stage B passes.

## Final source and machine qualification

- Source commit: `4153720cde8c6bd0004b8e69baeadf5254735abe`
- Android candidate CI run: `35550539216` — PASS
- design-lint run: `35550539365` — PASS
- CI artifact id: `10618495983`
- CI artifact name: `KINLINK-0.7.0-ci-build`
- Artifact ZIP SHA-256: `0deb11a13ba4ee2b8f15d96865741af3be42483d707e2814dabe5bef22717c09`

Machine PASS includes:
- fail-open API fence;
- manifest safety fence;
- background repeating-wakeup fence;
- unit/regression tests;
- Android lint;
- unsigned release-like candidate build;
- package/version identity proof;
- unsigned-state proof;
- telemetry migration simulation and durable-ledger/signer-contract checks.

## Unsigned CI candidate

- Package: `com.terminator364.kinlink`
- versionCode: `8`
- versionName: `0.7.0`
- Variant: `candidate-unsigned`
- Unsigned APK SHA-256: `3c08caec397d49c8ab623cacb8e3f749cdae4fb23289a8d53b4e14b8cf0fafb8`
- Unsigned APK size: `308944` bytes
- Build-manifest commit: `4153720cde8c6bd0004b8e69baeadf5254735abe`
- Public-CI unsigned-state fence: PASS

## Final stable-signed field candidate

- File: `KINLINK-0.7.0-FINAL-FIELD-CANDIDATE.apk`
- Signed APK SHA-256: `cea3468340a8f81dc38cc1ba09abb68e8f9ccc1634e223f275b0e92b083ef0bc`
- Size: `320260` bytes
- APK Signature Scheme v3: verified
- Signers: `1`
- Signer certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Canonical signer continuity: PASS

All 71 original APK ZIP entries retain identical CRC and uncompressed size after signing. The v3 signature is carried by the APK signing block; application payload entries were not changed.

## Field-qualification hardening in this final candidate

VersionCode 8 evidence is explicitly scoped so retained RC3 history cannot qualify 0.7.0.

Required positive receipts:
- `SELF_TEST_CORE_V8`
- `SELF_TEST_OBSERVER_CALLBACK_V8`
- `HANDOFF_OUTCOME_MOBILE_VALIDATED_V8`
- `HANDOFF_CELLULAR_TO_WIFI_V8`
- latest authoritative resource verdict = `RUNTIME_RESOURCE_GATE_PASS_V8`

Automatic terminal receipts:
- `FIELD_CANDIDATE_QUALIFIED_V8`
- `FIELD_CANDIDATE_BLOCKED_V8` when the latest resource verdict is blocking

A newer clean resource PASS can supersede an older BLOCKED receipt; the older evidence is retained rather than deleted.

Battery qualification explicitly treats charging sessions as INCONCLUSIVE. Power disconnect automatically starts a fresh bounded 30-minute resource window.

## Historical preliminary artifact

An earlier signed 0.7.0 artifact from commit `9a4a0c5...` was produced during pipeline validation but was marked `SUPERSEDED_BEFORE_DELIVERY`. It was never delivered or installed and is not eligible for field use.

## Stage A verdict

- MACHINE: PASS
- MIGRATION: PASS
- SIGNER_CONTINUITY: PASS

**Stage A: PASS — final candidate READY_FOR_FIELD.**

## Stage B remains pending

Canonical promotion remains forbidden until this exact signed candidate proves:
- FIELD_HANDOFF = PASS
- RESOURCE_QUALIFICATION = PASS

Until then, RC3 remains the canonical installer and rollback baseline.
