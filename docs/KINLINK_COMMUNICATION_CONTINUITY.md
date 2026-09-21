# KINLINK communication & continuity architecture v2

## Objective

A 25-minute tranche must finish like a normal conversation, not like an emergency recovery.

BCP lessons are used as a control-plane model, but Gmail remains KINLINK's current human delivery surface.

## Cadence

- 22 minutes useful work;
- 3 minutes normal close reserve.

## Normal flow

1. reconcile any prior incomplete delivery;
2. Gmail START;
3. provider START message ID + thread ID;
4. persist delivery key;
5. work + durable checkpoints;
6. minute 22: primary assistant enters close reserve;
7. persist CLOSE_INTENT;
8. send END as a reply in the START Gmail thread;
9. require provider END message ID;
10. persist CLOSED;
11. only then short ChatGPT pointer.

## Invisible redundancy

Post-nominal backups exist only so a platform/tool interruption cannot lose END.

They never preempt the primary close and never expose recovery jargon to the user.

Before any backup send:
- search the START thread;
- search the delivery key;
- if END exists, reconcile its message ID and close without duplicate mail.

## Delivery ledger

`.project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl` is append-only evidence.

Events include:
- START_ACKNOWLEDGED;
- WORK_CHECKPOINT;
- CLOSE_INTENT;
- END_ACKNOWLEDGED;
- CLOSED;
- internal reconciliation events.

The ledger never treats send intent as delivery proof.

## Writer/continuity discipline

- one active tranche owns communication close;
- product development uses its fenced work branch;
- exact-head CI belongs only to the SHA tested;
- next_atomic_action is always durable;
- interruption resumes from the last evidence anchor.

## User-visible contract

Normal result:
- START mail;
- full FIN mail in the same thread;
- ChatGPT says only to check Gmail with Kinshasa date/time.

Backup activation should be indistinguishable from a normal FIN mail unless the actual technical report itself contains a real product failure.
