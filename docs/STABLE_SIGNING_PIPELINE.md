# KINLINK stable signing pipeline

## Goal

Preserve Android update continuity without exposing the private signing key in the public repository.

## Canonical signer

Every promoted KINLINK APK after the M2 migration must verify against:

`2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`

The private signing material is stored only in the private Google Drive project area
`KINLINK/SIGNING`. Never commit the keystore or credential file to public GitHub.

## Promotion sequence

1. Build and test the candidate in GitHub Actions.
2. Download the exact CI APK and verify its CI hash/manifest.
3. Retrieve the private KINLINK signing material from the private Drive signing area.
4. Sign the candidate with the canonical KINLINK update key.
5. Verify the APK signature and signer certificate.
6. Compute the final signed APK SHA-256.
7. Replace `KINLINK/INSTALLER/KINLINK_LATEST.apk` in place.
8. Download the Drive copy again and require byte-for-byte equality with the verified local signed APK.
9. Keep exactly one APK in the installer folder.
10. Persist the final hash, signer fingerprint, source commit and CI run in `project_state.json`.

## Safety rules

- Never upload the private key or credentials to a public CI artifact or public repository.
- Never promote an APK signed by a disposable CI debug key.
- Never rotate the stable key merely to fix a build problem.
- If the stable key cannot be retrieved or verified, stop promotion instead of producing an incompatible update.
- Signing continuity is a release gate, not an optional polish step.
