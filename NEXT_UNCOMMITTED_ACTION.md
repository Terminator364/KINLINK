# NEXT_UNCOMMITTED_ACTION

## Mandatory communication gate

Resume with exactly:

`KINLINKGO`

Before substantial product work:
1. reload `.project-memory/ACTIVE_TRANCHE.json`, `.project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl`, and `.project-memory/FIELD_CANDIDATE_FREEZE.json`;
2. if ACTIVE_TRANCHE is CLOSED, open a NEW tranche and persist Gmail START ACK before product work;
3. never reuse a CLOSED tranche;
4. before any final ChatGPT app reply: checkpoint -> CLOSE_INTENT -> Gmail END -> provider ACK -> persist END_ACKNOWLEDGED;
5. final app closeout response is forbidden until END_ACKNOWLEDGED is durable.

## Communication incident remediation

The prior missing-END incident was caused by starting new substantial work while ACTIVE_TRANCHE still referenced an already CLOSED tranche.

Schema v4 now enforces:
- a new durable tranche for new work;
- closeout priority over further product mutation;
- a final-response END_ACK gate.

## Frozen field candidate

Installed phone:
- KINLINK 0.7.2 / versionCode 10.

Frozen successor:
- app versionName: `0.7.3-dev`
- field label: `0.7.3 final-like`
- versionCode: 11
- source head: `2aeac4cf801e60986c33347676e12310790d1d98`
- branch: `dev/0.7.3-final-like-ui-control`
- design-lint run `35626585191`: PASS
- Android run `35626585156`: PASS
- 36/36 rendered screenshots: assertions PASS + manual visual review PASS
- unsigned APK SHA-256: `b8c8f3e82f9b5503b8c91edf9b30d171586ce901bc75e71f77127be7d1296b78`
- signed APK SHA-256: `d268542a4e6d4a4a5869deddd71bf0aec32c46386d2193cf96da6b825e68877d`
- canonical signer SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Drive FIELD_CANDIDATE file id: `1NvwAVPKHMXzK801X6xE9t2wDj1GIBtIB`
- Drive readback SHA-256: exact match / PASS
- canonical INSTALLER: untouched.

0.7.2 staging history is archived under:
`KINLINK/FIELD_CANDIDATE/INSTALLED_SUPERSEDED_0.7.2`.

## Exact next human gate

Perform ONE in-place Android update using the exact 0.7.3 field candidate from Drive.

Rules:
- do NOT uninstall 0.7.2 first;
- do NOT install any CI/debug/unsigned APK;
- do NOT replace canonical INSTALLER yet;
- after installation, verify versionCode 11 + signer continuity + startup/UI + field telemetry/receipts before any canonical promotion.

No further production mutation is allowed while this candidate freeze remains active unless the freeze is explicitly invalidated first.
