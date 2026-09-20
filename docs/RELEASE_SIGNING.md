# KINLINK release signing

KINLINK updates must use one persistent Android signing certificate. The initial M0 debug APK was a field bootstrap using a temporary CI debug key; never distribute another CI debug APK as an update.

## Permanent rule

The signing keystore, its passwords, and any encoded copy remain outside GitHub and outside public Drive. GitHub Actions compiles and tests source only.

## One-time offline station

On the user's Windows PC, create the KINLINK release keystore locally and keep one offline protected backup. Use `New-KinlinkSigningKey.ps1` in `tools/signing`: it prompts for the two passwords without writing them into the script or a text file, refuses to overwrite an existing key, and prints the public SHA-256 certificate fingerprint.

The local signing station supplies these four values only to the local release build:

| Local value | Purpose |
|---|---|
| `KINLINK_KEYSTORE_PATH` | Path to the local keystore |
| `KINLINK_KEYSTORE_PASSWORD` | Keystore password |
| `KINLINK_KEY_ALIAS` | Release-key alias |
| `KINLINK_KEY_PASSWORD` | Release-key password |

No value above is committed, uploaded to GitHub, put into diagnostics, or placed in `KINLINK/INSTALLER`.

## Promotion gate

Before replacing the sole file in `KINLINK/INSTALLER`, verify:

1. package is `com.terminator364.kinlink`;
2. version code is greater than the installed version;
3. signing certificate matches the durable production certificate;
4. SHA-256 and build manifest are generated;
5. CI tests pass and one field update test succeeds.

The currently installed temporary-key M0 app requires one exceptional uninstall/reinstall to migrate to the durable production certificate. Once migrated, normal updates install over the app with the same signing key and preserve its data.
