# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

- Commit `5ece5aaa7cf1e2f0869b30165357b5d200d19905`: redesigned human cockpit.
- GitHub Actions run `35521808716`: **PASS** — unit tests and debug APK build.
- CI now compiles and tests only. No keystore, password, token, telemetry, or signing secret is sent to GitHub.

## Next action

Prepare the one-time **offline signing station** on the user's Windows PC:
1. create the persistent KINLINK release keystore locally and make one offline backup;
2. build the release APK locally with that keystore;
3. verify package/version/certificate/SHA-256;
4. replace the sole APK in Drive `KINLINK/INSTALLER` in place;
5. perform the exceptional migration from the temporary M0 debug signer;
6. thereafter, build all updates with the same local signing key.

Do not publish a CI debug APK as an update. The redesigned cockpit is CI-verified but field-unverified.
