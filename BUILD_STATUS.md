# KINLINK build status

## P0 field incident under investigation

Field evidence: after leaving home Wi-Fi, the user reported that mobile data no longer passed normally.
Current code has no VPN, no process/network binding, no persistent network request and no API capable of toggling mobile data.
Causality is therefore not proven.

## Hardening decision

RC2 field promotion is suspended until the handoff regression gate passes.

New fail-open invariant:
- KINLINK never calls Android connectivity-validation reporting APIs;
- no positive or negative probe result may alter Android's network-validation state;
- automatic recovery remains Wi-Fi-only;
- cellular transitions always result in NO_ACTION;
- only bounded Wi-Fi metric refresh remains permitted.

## Existing machine evidence

- RC2 source baseline: `7124e1d1cc9b3430f24af74fc2501371c182c25a`
- RC2 CI: PASS
- Previously promoted signed APK: rollback only; do not field-promote after the new incident.

## Next gate

Run full CI/regression after the P0 handoff hardening. Promote no installer until PASS.
