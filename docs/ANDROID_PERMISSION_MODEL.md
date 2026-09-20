# Android Permission Model

Permissions are capability gates, not all-or-nothing setup blockers.

## Core
- INTERNET
- ACCESS_NETWORK_STATE
- foreground-service capabilities as required
- VPN user consent when Stabilizer is enabled

## Smart diagnostics
- Wi-Fi/nearby permissions where Android version requires them
- notifications
- Usage Access (special settings grant) for richer per-app/network accounting
- phone/SIM state permissions only when user enables Carrier Wallet features

## Optional advanced diagnostics
- location-related permission only when required by Android for active Wi-Fi scanning workflows
- USSD/call permission only for explicit Carrier Wallet adapters

## UX contract
The setup wizard:
- explains why each capability is requested;
- opens the exact settings page where possible;
- verifies the grant on return;
- continues with reduced capability if optional permission is denied.
