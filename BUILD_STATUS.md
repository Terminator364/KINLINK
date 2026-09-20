# KINLINK build status

## M2 promoted installer — VERIFIED

- Product version: `0.2.0-m2`
- Binary source commit: `6010398a7f64e62d301c3e5b4317c6ce5f927b48`
- GitHub Actions run: `35527033841`
- CI result: **PASS**
- Unit tests: **PASS**
- Debug APK build: **PASS**
- Design lint: **PASS**
- APK SHA-256: `7960160c137ff9776ba5ea694bdd961a7ad2a645ac2f3c7a757e46b4cf8b1cc9`
- APK size: `6450808` bytes
- Drive installer: `KINLINK/INSTALLER/KINLINK_LATEST.apk`
- Drive file id: `1OE47FFE4SmIylMw4MGHdpYu4drJxY9Bo`
- Installer folder readback: exactly one APK.

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

The next real gate is one target-phone installation and field use of the promoted APK.
