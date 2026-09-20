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
