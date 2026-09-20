# Build and Update Strategy

## Repository
Public source repository is acceptable only if personal telemetry, secrets, Wi-Fi identifiers, carrier information, signing keys, and diagnostics never enter the repo.

## CI
Public GitHub Actions builds and tests unsigned/debug artifacts.
Release signing remains outside the public repository with the persistent KINLINK signer.

CI must fail when production source or manifest reintroduces forbidden network-ownership behavior, including:
- process network binding;
- persistent requestNetwork ownership;
- framework connectivity reporting;
- unqualified VpnService;
- network-changing privileged permissions.

## Channels
- field baseline: last machine + field validated signed build
- development: non-promotable DEV line
- release candidate: consolidated, machine-qualified candidate only

## Version identity
Never build materially different binaries under the same versionCode/versionName.
The installed field baseline remains immutable while development continues on a strictly higher versionCode.

## Update manifest
Each release must publish:
- versionCode;
- versionName;
- minSdk / targetSdk;
- source commit;
- SHA-256;
- APK size;
- signer certificate SHA-256;
- release timestamp;
- compatibility notes;
- telemetry/schema migration version;
- machine-gate verdict;
- field-gate status.

## Post-update self-test
On first launch after update:
- validate database migrations;
- validate observer callbacks;
- validate safe policy and RecoveryMode load;
- validate lifecycle/startup receipts;
- validate forbidden-network-ownership invariants;
- confirm telemetry ledger is writable;
- retain last-known-good policy configuration.

VPN/TUN capability is not part of the normal self-test because it is production-disabled until separate qualification.

## Canonical Drive promotion
- sign with the persistent KINLINK signer;
- verify signer continuity;
- update the canonical Drive file in place;
- download it back;
- verify byte-for-byte SHA-256 and APK signature;
- delete staging objects only after readback PASS;
- do not replace a proven field baseline with a DEV artifact.
