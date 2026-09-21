# KINLINK communication & continuity architecture v3

Communication continuity is P0. Delivery security is checked before product instructions.

## Start
1. reconcile prior tranche;
2. load delivery-security policy;
3. Gmail START;
4. require provider message/thread ID;
5. apply KINLINK label;
6. persist START_ACKNOWLEDGED;
7. only then substantive work.

## End
1. stop new product mutations;
2. persist exact checkpoint + CLOSE_INTENT;
3. Gmail END;
4. require provider ACK/message ID;
5. persist END_ACKNOWLEDGED/CLOSED;
6. only then final short app pointer.

A CLOSED tranche may never be reused.

## Security interruption
Platform/security controls are never bypassed. A hold is an interruption boundary: persist state, stop the blocked/risky action, keep idempotency, resume later from the durable proof anchor.

## APK delivery binding
No install instruction is valid unless `.project-memory/DELIVERY_SECURITY_POLICY.json` passes. The human path exposes one authorized APK only. Rollbacks and stale candidates stay outside it.
