# KINLINK build status

## M2 stable installer — VERIFIED

- Product version: `0.2.0-m2`
- App source commit used for the promoted binary: `6010398a7f64e62d301c3e5b4317c6ce5f927b48`
- GitHub Actions verification run: `35527033841`
- CI result: **PASS**
- Unit tests: **PASS**
- Debug APK build: **PASS**
- Design lint: **PASS**
- Stable APK SHA-256: `ca09e40d0ad5d8e3bec8ddfe56e80bdc6a2449a653ace75f7877385e697d12a7`
- Stable signing certificate SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Stable APK signature scheme verified: **v3**
- Stable APK size: `6466127` bytes
- Drive installer: `KINLINK/INSTALLER/KINLINK_LATEST.apk`
- Drive file id: `1OE47FFE4SmIylMw4MGHdpYu4drJxY9Bo`
- Drive readback: byte-identical to the locally verified stable APK.
- Installer folder readback: exactly one APK.

## Signing continuity

A persistent KINLINK update key now exists privately in Google Drive under `KINLINK/SIGNING`.
The private key and credentials are intentionally absent from public GitHub.
All future promoted APKs must be signed with this same key and verified against certificate
`2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`.

The previously installed M1 was signed by a disposable CI debug certificate, so the transition to
this stable signing baseline requires one final uninstall/reinstall. After that migration, future
updates must preserve the stable key and should install over the existing app.

## What M2 actually does

- event-driven passive connectivity observation;
- bounded privacy-safe local telemetry;
- explicit Wi-Fi-only micro-diagnostic;
- explicit Android connectivity re-evaluation hint;
- explicit bandwidth-metric refresh request;
- Android 15/16 foreground-service hardening using declared `specialUse`;
- strict no-mobile-probe, no-speedtest and fail-open behavior.

## Claims deliberately NOT made yet

M2 is not yet claimed to provide whole-device throughput acceleration, system-wide DNS optimization,
VPN/TUN traffic steering, or field-proven latency reduction. Those require a later measured data-plane milestone.

## Current gate

One target-phone migration to the stable signing baseline, then one explicit Wi-Fi optimization field check.
