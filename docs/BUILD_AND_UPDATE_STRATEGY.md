# Build and Update Strategy

## Repository
Public source repository is acceptable only if personal telemetry, secrets, Wi-Fi identifiers, carrier information, signing keys, and diagnostics never enter the repo.

## CI
Public GitHub Actions builds and tests unsigned/debug artifacts.
Release signing remains outside the public repository unless a later audited key-management design explicitly changes this.

## Channels
- stable
- beta

## Update manifest
Each release must publish a small manifest with:
- versionCode
- versionName
- minSdk / targetSdk
- SHA-256
- APK size
- release timestamp
- compatibility notes
- telemetry/schema migration version

## Post-update self-test
On first launch after update:
- validate database migrations
- validate observer callbacks
- validate safe policy load
- validate VPN capability without forcing permanent takeover
- retain last-known-good policy configuration
