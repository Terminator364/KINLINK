# KINLINK communication delivery state machine

Status: CANONICAL COMMUNICATION SAFETY CONTRACT v2
Cadence: 25 minutes = 22 minutes useful work + 3 minutes normal close reserve

## Core correction

The backup sentinel must **not** become the normal closer.

Normal path ownership:
`PRIMARY_ASSISTANT -> CLOSE_INTENT -> Gmail END -> provider ACK -> CLOSED`

Backup path:
`only after nominal window -> search for existing END -> reconcile or send the same normal END`

This removes the confusing pattern where the user receives a visible "recovery/rattrapage" email instead of the expected ordinary FIN TRANCHE.

## Separate facts

BCP-inspired model:

- **work_state**: engineering progress;
- **delivery_state**: START/END transport proof;
- **close_owner**: PRIMARY_ASSISTANT / SHADOW_BACKUP / HARD_GUARD.

Technical PASS does not imply communication CLOSED.

## Idempotency key

Every tranche gets a stable `delivery_key` and START Gmail thread.

Before END:
1. persist `CLOSE_INTENT_PERSISTED`;
2. bind the END report to the delivery key;
3. send END as a reply to START;
4. persist the returned Gmail message ID.

If the process dies after Gmail accepted END but before GitHub persisted its ID, the next closer:
- searches the START thread and delivery key first;
- reuses the existing END receipt;
- never sends a duplicate.

## Normal close

Minute 22 starts the three-minute reserve.

The primary assistant:
1. stops new product mutations;
2. reads exact branch/head/CI truth;
3. persists next atomic action;
4. persists CLOSE_INTENT;
5. sends ordinary `KINLINK — FIN TRANCHE 25 MIN ... [delivery_key]`;
6. requires provider message ID;
7. applies KINLINK label;
8. persists END_ACKNOWLEDGED/CLOSED;
9. only then emits the app pointer.

## Backup semantics

Backup is post-nominal only:
- shadow backup: after minute 25;
- hard guard: later still.

Both first search Gmail for an already-delivered END.

If they must send:
- same normal FIN subject/body semantics;
- same START thread;
- same delivery key;
- no user-visible "RECOVERY", "RATTRAPAGE", "WATCHDOG" wording.

Recovery classification remains internal to the ledger.

## Crash windows

### Before CLOSE_INTENT
Latest durable work checkpoint is authoritative.

### After CLOSE_INTENT but before Gmail ACK
The next closer sends one END for the delivery key.

### After Gmail ACK but before GitHub receipt
The next closer searches Gmail and persists the existing message ID without resending.

## BCP principles imported

- append-only delivery ledger;
- work state != delivery state;
- idempotency keys;
- durable next atomic action;
- exact evidence anchor;
- single-writer ownership;
- retry without duplicate downstream effect;
- no fake progress;
- user does not need to ping merely to obtain closure.
