# NEXT_UNCOMMITTED_ACTION

## RC3 promotion pipeline

1. Wait for full RC3 CI (policy tests + source fence + manifest fence + APK build).
2. Inspect failures automatically; repair before any user action.
3. Fetch RC3 CI artifact and verify its build manifest/hash.
4. Stable-sign with the existing KINLINK signer.
5. Verify signer continuity.
6. Replace only the canonical Drive installer and perform byte-for-byte readback.
7. Then request one in-place field update.

## Human action

None until all machine gates above are complete.
