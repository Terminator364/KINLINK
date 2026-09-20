# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

- Commit `5ece5aaa7cf1e2f0869b30165357b5d200d19905`: redesigned human cockpit.
- GitHub Actions run `35521808716`: **PASS** — unit tests and debug APK build.
- The installed M0 APK was signed by an ephemeral CI debug key. It cannot be updated safely by a later CI debug APK because Android requires the same signing certificate.

## Next action

Establish a persistent release signing key outside Git, store its encrypted value only as GitHub Actions secrets, then:
1. configure a `release` build to read those secrets;
2. increment `versionCode` to 2 and version to `0.1.1-m0`;
3. build, verify package/version/certificate/SHA-256;
4. replace the single APK in Drive `KINLINK/INSTALLER` in place;
5. perform one explicit field update test.

Do not deliver a new APK to the user before the signing identity is durable. The current UI build is CI-verified but field-unverified.
