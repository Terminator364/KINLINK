# KINLINK release signing

KINLINK updates must use one persistent Android signing certificate. The initial M0 debug APK was only a field bootstrap and used a temporary CI debug key; do not distribute a replacement debug APK as an update.

## One-time GitHub setup

Create one Android keystore locally and keep an offline backup outside Git and outside public Drive. Then add these **GitHub Actions secrets** in the KINLINK repository:

| Secret | Value |
|---|---|
| `KINLINK_KEYSTORE_BASE64` | Base64 of the keystore file |
| `KINLINK_KEYSTORE_PASSWORD` | Keystore password |
| `KINLINK_KEY_ALIAS` | Alias of the release key |
| `KINLINK_KEY_PASSWORD` | Key password |

The workflow builds a release APK only when all four are present. It never writes a keystore, password, token, SSID, or user telemetry into Git.

## Release gate

Before replacing the sole file in `KINLINK/INSTALLER`, verify:

1. package is `com.terminator364.kinlink`;
2. version code is greater than the installed version;
3. signing certificate matches the installed production certificate;
4. SHA-256 and build manifest are generated;
5. CI tests pass; then field-test the update once.

The currently installed temporary-key M0 app will require one exceptional uninstall/reinstall to move to the durable production signing certificate. After that migration, normal updates install over the existing app and preserve data.
