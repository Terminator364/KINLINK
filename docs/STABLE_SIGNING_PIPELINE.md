# KINLINK stable signing pipeline

## Goal

Preserve Android update continuity without exposing the private signing key in the public repository.

## Canonical signer

Every promoted KINLINK APK after the M2 migration must verify against:

`2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`

The private signing material is stored only in the private Google Drive project area
`KINLINK/SIGNING`. Never commit the keystore or credential file to public GitHub.

## Promotion sequence

1. Build and test the release-like **unsigned** candidate in GitHub Actions using the `candidate` Gradle variant.
2. Download the exact CI artifact and verify the artifact digest, APK SHA-256 and build manifest.
3. Retrieve the private KINLINK signing material from the private Drive signing area.
4. On the private signing station, run `tools/signing/Sign-KinlinkCandidate.ps1` with the expected CI APK SHA-256.
5. The signer must verify zip alignment, refuse an input-hash mismatch, sign with the private key via environment-backed passwords, verify the APK signature and require the canonical signer fingerprint.
6. Persist the generated non-secret signing receipt containing input hash, output hash and signer certificate SHA-256.
7. Install that exact stable-signed artifact as the single field candidate; do **not** replace the canonical RC3 file yet.
8. Collect FIELD_HANDOFF and RESOURCE_QUALIFICATION evidence on that exact artifact.
9. Only after all five release gates PASS, replace `KINLINK/INSTALLER/KINLINK_LATEST.apk` in place.
10. Download the Drive copy again and require byte-for-byte equality with the verified signed APK.
11. Keep exactly one APK in the installer folder.
12. Persist the final hash, signer fingerprint, source commit and CI run in `project_state.json`.

## Safety rules

- Never upload the private key or credentials to a public CI artifact or public repository.
- Never promote an APK signed by a disposable CI debug key.
- Never rotate the stable key merely to fix a build problem.
- If the stable key cannot be retrieved or verified, stop promotion instead of producing an incompatible update.
- Signing continuity is a release gate, not an optional polish step.


## Two-stage safety

Stage A allows one signed field candidate after MACHINE + MIGRATION + SIGNER_CONTINUITY pass.

Stage B allows canonical promotion only after FIELD_HANDOFF + RESOURCE_QUALIFICATION also pass.

This avoids both unsafe early promotion and a chain of small beta installs.
