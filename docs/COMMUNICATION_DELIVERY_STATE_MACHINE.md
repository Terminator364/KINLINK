# KINLINK communication delivery state machine

Status: CANONICAL COMMUNICATION SAFETY CONTRACT
Cadence: 25 minutes = 22 minutes useful work + 3 minutes mandatory closeout reserve

## Why this exists

A technically successful KINLINK tranche is not complete if the END report is not actually delivered.

KINLINK therefore separates:
- **work state** — what engineering was committed/tested;
- **delivery state** — whether START/END communication was provider-acknowledged.

This follows the stronger BCP pattern: durable progress must survive an interrupted ChatGPT turn without requiring the user to send “continue”, “eh oh”, or another recovery ping just to obtain the missing report.

## State machine

`START_REQUIRED -> START_PROVIDER_ACKED -> WORKING -> CLOSEOUT_ARMED -> CHECKPOINT_DURABLE -> END_GENERATED -> END_PROVIDER_ACKED -> CLOSED`

A tranche is not CLOSED merely because engineering work stopped.

## START handshake

Before substantive work:
1. repair any previously unclosed tranche first;
2. send Gmail START;
3. require returned provider message ID;
4. apply Gmail label KINLINK;
5. persist tranche id + START message ID;
6. arm two one-shot guards;
7. only then begin engineering.

If Gmail START has no provider acknowledgement, substantive work does not begin.

## Two independent one-shot guards

### Closeout watchdog

Armed at tranche start for the 22-minute useful-work boundary.

If the tranche is already CLOSED with verified END message ID: no-op.

Otherwise it:
- stops new technical mutations;
- reads latest durable GitHub/CI state;
- persists a closeout checkpoint;
- sends complete Gmail END;
- requires provider message ID;
- closes the tranche;
- only then allows the short app pointer.

### Hard END guard

Armed for the nominal 25-minute boundary.

It is a recovery path, not ordinary execution.

If END is already provider-acknowledged: no-op.

If not:
- no product mutation is allowed;
- missing END is repaired from latest durable state;
- provider message ID is required;
- CLOSED state is persisted before any app pointer.

## 22 + 3 rule

Minutes 0–22:
- engineering;
- audit/counter-audit;
- CI;
- durable micro-checkpoints.

Final 3 minutes:
- **no new product mutation**;
- capture exact branch/head/CI truth;
- persist next atomic action;
- send END;
- verify provider acknowledgement;
- persist CLOSED;
- disable pending watchdogs.

If mutation/tool budget is exhausted earlier, closeout begins early rather than risking another missing END.

## Interruption behavior

If the main assistant turn is stopped, rate-limited, tool-interrupted, or otherwise cut:
- committed work remains authoritative;
- the one-shot guards can still close communication from durable state;
- the next assistant turn MUST first inspect delivery state;
- an unclosed prior tranche is repaired before any new engineering or user-facing technical answer.

No platform safeguard is bypassed. The design only makes interruption non-destructive.

## Delivery evidence

Each tranche requires:
- `gmail_start_message_id`;
- `start_provider_ack=true`;
- durable work checkpoint;
- closeout marker;
- `gmail_end_message_id`;
- `end_mail_verified=true`;
- CLOSED state.

## Relation to BCP

Imported BCP concepts:
- Gmail START/END provider-ack handshake;
- one-shot END watchdog;
- durable progress journal;
- separate completion vs delivery state;
- next atomic action on interruption;
- single-writer/exact-head evidence discipline.

KINLINK keeps this narrower than BCP: it does not need the full Telegram mission cockpit merely to guarantee tranche closure.
