# AGENTS.md — KINLINK Engineering Governor

## Product priority
Networking safety and evidence outrank feature count.

## Hard rules
1. Never ship a network mutation without a fail-open path.
2. Never add automatic volumetric tests on metered mobile data.
3. Never let adaptive learning override hard data/battery/thermal budgets.
4. Never log payload contents, secrets, full SIM identifiers, or plaintext Wi-Fi credentials.
5. LAN health and Internet health are separate concepts.
6. A successful build is not field verification.
7. Every automatic action must have an observable reason and outcome.
8. Remote GitHub Actions are checkpoint evidence, not the ordinary development loop.
9. Workflows default to `workflow_dispatch` until the owner explicitly authorizes automatic triggers.
10. Field findings must become regression tests or durable design evidence.

## Current build policy
M0 exists to prove Observer Core, telemetry, and UI contracts. Do not add VPN/TUN interception before M0 state/ledger tests are stable.
