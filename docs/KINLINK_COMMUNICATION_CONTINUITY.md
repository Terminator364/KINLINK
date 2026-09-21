# KINLINK communication & continuity architecture

## Imported lessons from BCP

KINLINK now adopts the parts of BCP that directly address the repeated communication failures:

- **START/END provider handshake**: a Gmail send request is not enough; the provider-returned message ID is persisted.
- **Work state != delivery state**: technical work may be complete while the END report is still undelivered.
- **One-shot closeout watchdog**: armed at each tranche start and fires at minute 22.
- **Hard END guard**: independent second one-shot guard at minute 25.
- **Durable next atomic action**: interruption resumes from the next uncommitted action, never by restarting the project.
- **No user ping required**: an interrupted main turn must still be closable by the watchdog path.
- **Provider-acknowledged END before app output**: the ChatGPT app is pointer-only after successful END.
- **Writer/branch isolation**: product experimentation stays on a work branch while installed/canonical field state remains frozen.

## 22 + 3 contract

A 25-minute tranche is not 25 minutes of unrestricted engineering.

- minutes 0–22: useful technical work;
- minutes 22–25: reserved closeout only.

At closeout:
1. stop new product mutations;
2. persist exact branch head / CI / next atomic action;
3. send complete Gmail END;
4. apply KINLINK label;
5. require Gmail provider acknowledgement / message ID;
6. persist CLOSED;
7. only then allow ChatGPT pointer output.

## Two independent facts

Every tranche carries:

### Work state
- WORKING
- CHECKPOINTED
- HUMAN_GATE
- FAILED_SAFE

### Delivery state
- START_ACKNOWLEDGED
- END_SEND_PENDING
- END_ACKNOWLEDGED
- CLOSED
- RECOVERY_REQUIRED

A technical PASS with an unacknowledged END is **not** a closed tranche.

## Interruption recovery

If the main assistant turn is interrupted after START:
- closeout watchdog at +22 min inspects the durable state;
- if not CLOSED, it sends the END from the latest durable checkpoint;
- hard guard at +25 min repeats the check;
- if the first guard succeeded, the second does nothing;
- if both were delayed, the next KINLINK turn must repair RECOVERY_REQUIRED before any new engineering work.

This is intentionally idempotent.

## BCP boundary

KINLINK does not need the full BCP control plane to benefit from these rules.
The current implementation uses:
- GitHub durable state;
- Gmail provider receipts;
- ChatGPT one-shot automations;
- exact-head CI receipts.

A future BCP integration can become the canonical sentinel, but KINLINK continuity must remain correct even before that integration is field-proven.
