# KINLINK delivery security & installation

Status: P0 CANONICAL DELIVERY CONTRACT

Normal human installation uses one folder only: `KINLINK/INSTALLER`.
It contains one APK only: `KINLINK_INSTALL_NOW.apk`.

All pre-install gates must pass:
1. package = `com.terminator364.kinlink`;
2. candidate versionCode > known installed versionCode;
3. canonical signer exact match;
4. signed APK SHA-256 exact match;
5. Drive readback exact match;
6. exact-head UI/release gates pass;
7. no rollback/stale APK in normal install path;
8. no signing secret in public GitHub/Gmail/human delivery folders.

Current accepted artifact:
- installed v10;
- candidate v11;
- SHA-256 `d268542a4e6d4a4a5869deddd71bf0aec32c46386d2193cf96da6b825e68877d`;
- Drive file ID `1dj2j2zhLmUPb63NVIaMx0jElpNdb04ky`.

Rollback is preserved under `ROLLBACK_CANONICAL_DO_NOT_INSTALL`; a lower versionCode is never offered as an in-place update.

Platform/security controls are never bypassed. If blocked, persist a safe checkpoint and resume from proof after the control clears.
