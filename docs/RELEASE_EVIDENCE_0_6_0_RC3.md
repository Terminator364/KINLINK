# KINLINK 0.6.0-rc3 — Release Evidence

## Source
- Canonical source commit: `dc7555b784562a059e16107c574839a66dff89cd`
- Android CI run: `35536189018`
- CI conclusion: PASS
- design-lint conclusion: PASS

## CI artifact
- Artifact id: `10613226029`
- Artifact name: `KINLINK-rc3-ci-build`
- Artifact ZIP digest: `sha256:02491594f46f2183859f4d879fdc78cb99c1407c4e7e89af9560bfcd4f3811f1`
- APK SHA-256 inside CI artifact: `b24c8aab5a34039dc1a982a9b5f4b65cb9de0aec259f1f380cf834b1ef362668`
- Manifest version: `0.6.0-rc3`
- Manifest commit: `dc7555b784562a059e16107c574839a66dff89cd`

## Stable-signed candidate
- File: `KINLINK-0.6.0-rc3-signed.apk`
- Size: `6584911` bytes
- SHA-256: `4fb4ac36765b6d3728194bf7884f3c98afff625f4a5bc0637b825db0703a700c`
- APK Signature Scheme v3: verified
- Signers: 1
- Certificate DN: `CN=KINLINK Update Key, OU=Personal Android, O=KINLINK, L=Kinshasa, C=CD`
- Certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`

## Safety gates passed
- no production `bindProcessToNetwork`
- no production `requestNetwork` ownership
- no `reportNetworkConnectivity`
- no production `VpnService`
- manifest rejects network-changing permissions
- automatic recovery = AUTOMATIC + Wi-Fi only
- cellular = observation-only
- Android lint = PASS
- unit/regression tests = PASS
- APK build = PASS

## Distribution state
The previously promoted Drive installer remains intact until a byte-safe replacement path succeeds.
The signed RC3 candidate is ready. Drive replacement is not marked PASS until upload + byte-for-byte readback are proven.
