# Mobile Vault and Carrier Wallet

## Mobile Vault
A hard economic safety boundary around metered traffic.

Required envelopes:
- total plan estimate
- protected reserve
- normal mobile allowance
- rescue allowance
- critical-interactive allowance

The learner can recommend changes but cannot spend outside hard limits.

## Sources of truth
Carrier Wallet reconciles:
1. KINLINK flow ledger,
2. Android network statistics where permitted,
3. optional carrier USSD adapter,
4. user-entered plan/expiry data.

Every balance value stores:
- value
- source
- timestamp
- confidence
- adapter version

## Carrier adapter rules
- carrier-specific and versioned;
- parser failure => BALANCE_UNKNOWN, never fake zero/non-zero;
- USSD errors must never block connectivity;
- no background repeated USSD polling;
- queries are rate-limited and policy-controlled.
