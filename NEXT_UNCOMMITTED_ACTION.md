# NEXT_UNCOMMITTED_ACTION

Resume with `KINLINKGO`.

Mandatory first step: reconcile communication state. If K25-09 is not CLOSED, finish its END/ACK before any product mutation.

## Canonical scope

Load A + B + C, not recent traceability alone.

- A: recovered original preconception anchor `c565b6f1164d327caaa17e1de47d1cf48ed3c885`
- B: depth B3, B31-B80 in `docs/B_EXPANSION_V3.md` + `.project-memory/B_EXPANSION_LEDGER.json`
- C: `.project-memory/PRODUCT_EVOLUTION_C.json`
- macro denominator: 80
- conservative macro maturity: 44.8%
- B-depth items: 50; they do not change the macro denominator without explicit deduplication review.

Memory/continuity reads must also include:
- `.project-memory/MEMORY_POLICY_V2.json`
- `.project-memory/PROJECT_CHRONICLE.jsonl`
- `.project-memory/SUPERSESSION_LEDGER.json`
- `.project-memory/DEAD_END_REGISTRY.json`
- `.project-memory/COMMUNICATION_WATCHDOG_POLICY.json`
- `.project-memory/CANONICAL_CONTEXT_PACK.json`

## Communication watchdog

Exactly one automation: **KINLINK Comms Watchdog**.
It repairs stale/missed END closure idempotently. A WORKING tranche older than 35 minutes is treated as interrupted, closed from durable facts only, and never auto-reopened into an orphan START. Foreground work must send a fresh START.

## Active 0.8 successor

- branch: `dev/0.8.0-integrated-truth-and-control`
- exact head: `c6fb86ec8ef075f7e0cbdfd9cf0f04b8954088f1`
- design-lint run 35652449660: PASS
- Android run 35652449928: IN PROGRESS at close intent
- previous c8eb Android run 35651559235: PASS
- manual visual audit rejected one proof artifact: `technical-details-start` still captured the top page because the scroll ran before expanded layout settled.
- c6fb fixes that by waiting for layout, scrolling to the actual detail body, and asserting it lands near the viewport top.
- 45 screenshots expected.

## Exact next engineering action

1. poll Android run 35652449928;
2. if PASS, fetch `KINLINK-0.8.0-responsive-ui-proof`;
3. manually counter-audit all 45 screenshots, especially xl-font mobile-data dialog and technical-details-start/bottom;
4. reject any clipping, overlap, misleading quality language, raw enum leakage, or proof screenshot that does not show what it claims;
5. if clean, continue major A+B+C implementation blocks (not a micro-beta);
6. no field install/sign/Drive promotion until a coherent integrated batch is ready.

Communication: Gmail START provider ACK before substantive work; Gmail END provider ACK + durable CLOSED before final app closeout.
