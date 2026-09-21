# KINLINK W2 — Mobile Vault durable plan store

The Mobile Vault plan model is now persistable locally with an explicit schema version.

Stored fields:
- total plan bytes;
- optional expiry epoch;
- protected reserve;
- rescue allowance;
- critical-interactive allowance;
- updated-at timestamp;
- schema version.

Privacy boundary:
- no phone number;
- no IMSI/ICCID/IMEI;
- no carrier secret;
- no account credential.

The legacy daily caution limit is **not** auto-migrated because a daily threshold is not semantically equivalent to a carrier plan/cycle budget.

Human-facing MB conversion uses decimal MB (1 MB = 1,000,000 bytes) so future UI can speak MB/GB consistently with normal mobile-data plans.

This store does not yet claim per-SIM attribution or operator balance. Those remain B91-B100 gates.
