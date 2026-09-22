# NEXT_UNCOMMITTED_ACTION

Resume with `KINLINKGO`.

## P0 communication first
1. verify RESUME_CAPSULE and fresh communication state;
2. K25-20 is CLOSED with Gmail END ACK `1a0c87395f2b17c4`;
3. a fresh Gmail START is required before new product work;
4. keep v6 cadence: 20 min useful work + 5 min hard-close reserve.

## Canonical scope
- A+B+C: **80 macro capabilities**
- B5: **B31-B100**
- maturity: **46.0%** — deliberately unchanged
- W2 active
- installed baseline: **0.7.3-dev / versionCode 11**
- development line: **0.8.0-dev / versionCode 12**
- no micro-beta / no phone reinstall chain

## Newly proven W2 state — B95
Exact head:
`5a45fb3ec5d4d82960b7687fdd75836e6770ccb4`

Runs:
- design-lint `35710089427`: **PASS**
- Android `35710089508`: **PASS**
- CI build artifact: `10686770362`
- responsive UI proof artifact: `10686325691`

Machine-proven semantics:
- default-data and active-data are distinct facts with independent generations;
- first available observation initializes a generation without exposing identifiers;
- a default-data change alone does not invalidate active-data-bound evidence;
- an active-data change invalidates prior active-generation evidence;
- active-data observation loss => `ACTIVE_DATA_UNKNOWN`;
- reappearance after UNKNOWN creates a new generation so stale evidence cannot revive;
- `SAME/DIFFERENT` is only claimable when both sides are observable;
- no subId, SubscriptionManager, TelephonyManager or telephony permission is activated.

## Exact next W2 block — B96
1. model NetworkStats usage callbacks as **advisory/process-live only**;
2. missed callback must never mean zero usage;
3. after process death/restart, Mobile Vault must require fresh reconciliation before spending decisions;
4. persist plan state, but do not persist callback continuity as truth;
5. write pure process-kill/restart and missed-callback tests first;
6. no Android usage-callback activation yet;
7. no permission expansion;
8. no signing, Drive staging or phone install.

## Communication invariant
- Gmail START before substantive work.
- Gmail END before normal app closeout.
- END provider ACK must be persisted before final app response.
