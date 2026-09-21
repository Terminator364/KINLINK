# Telemetry and Diagnostics

## Privacy model
Local-first. Do not store payload contents, message text, auth tokens, full SIM identifiers, or plaintext Wi-Fi secrets.
Network identifiers should be pseudonymized for longitudinal matching.

## Event ledger
Each durable event contains:
- event_id
- monotonic timestamp + wall timestamp
- boot/session id
- app/engine version
- transport and capabilities
- LAN/WAN state
- failure-domain hypothesis + confidence
- relevant RTT/jitter/loss/stall observations
- budget state
- battery/thermal state
- decision + reason
- pre-action metrics
- post-action metrics
- verdict: beneficial / neutral / harmful / unknown
- rollback result when applicable

## Diagnostic export
ZIP layout:
- manifest.json
- summary.json
- events.jsonl
- decision_ledger.jsonl
- failure_clusters.json
- carrier_wallet.json
- energy_summary.json
- crashes.jsonl

## Weekly report
Human-readable summary generated locally:
- contexts used
- brownouts and outages
- likely failure-domain distribution
- interventions and success rate
- rollback count
- mobile bytes by class
- mobile budget incidents
- battery/thermal impact estimate
- unresolved clusters


## 0.7.1 structured diagnostic core bundle

The user-initiated diagnostic export now produces a local ZIP bundle instead of only one TXT file.

Current entries:
- `manifest.json`
- `summary.json`
- `recent_actions.jsonl`
- `report.txt`

Privacy contract:
- no SSID;
- no SIM identifier;
- no raw IP address;
- no raw gateway/interface name;
- no payload;
- no password/token;
- only topology booleans, aggregate state and bounded KINLINK receipts.

The export remains local and user-initiated. It performs no background upload and selects no recipient automatically.

This is a meaningful subset of the larger canonical diagnostic ZIP design. Remaining future entries such as failure clusters, carrier wallet, energy summary and crash history are not falsely claimed implemented yet.
