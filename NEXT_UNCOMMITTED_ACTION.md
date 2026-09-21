# NEXT_UNCOMMITTED_ACTION

## Mandatory communication gate

Resume with exactly:

`KINLINKGO`

Before substantial product work:
1. reload `.project-memory/ACTIVE_TRANCHE.json` and `.project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl`;
2. if ACTIVE_TRANCHE is CLOSED, open a NEW tranche and persist Gmail START ACK before product mutations;
3. never reuse a previously CLOSED tranche for new work;
4. before any final ChatGPT app reply: checkpoint product state, persist CLOSE_INTENT, send Gmail END, verify provider ACK, persist END_ACKNOWLEDGED;
5. final app reply is forbidden while delivery_state is not CLOSED / END_ACKNOWLEDGED.

The app chat may contain progress updates while work is active, but not a final/closeout answer.

## Communication incident fixed

The previous session sent Gmail START message `1a0c4b5a1a669b91` but produced a final ChatGPT reply without Gmail END.
Root cause: new work had started while durable `ACTIVE_TRANCHE` still referenced the already-closed tranche `KINLINK-2026-09-21-1631-K25-02`.

Remediation now persisted:
- ACTIVE_TRANCHE schema v4;
- mandatory new-tranche gate;
- final-response END_ACK gate;
- closeout priority over additional product work;
- incident recorded in communication ledger.

Current active recovery tranche:
- tranche: `KINLINK-2026-09-21-COMM-RECOVERY-K25-03`
- delivery key: `K25-20260921-COMMRECOVERY-03`
- Gmail START ACK: `1a0c4cf26258ae7d`

## Product state

Installed target phone:
- KINLINK 0.7.2 / versionCode 10;
- usable, but P0 UX-invalid as near-final because target-phone screenshots showed responsive header compression.

Current engineering line:
- branch: `dev/0.7.3-final-like-ui-control`
- exact head: `c98aaf23ed2818392d7330efb8785eca36befd2a`
- current Android run: `35625701977`
- design-lint: PASS
- unit tests: PASS
- Android lint: PASS
- unsigned candidate build: PASS
- candidate package identity/unsigned fence: PASS
- rendered responsive matrix: IN_PROGRESS at checkpoint

## Responsive qualification already established

The matrix covers:
- 3 screen/font configurations;
- 6 representative KINLINK states;
- top + bottom captures = 36 expected screenshots;
- clipping/ellipsis detection;
- suspicious narrow-column detection;
- sibling overlap detection;
- 48dp tap-target checks;
- collapsed technical details;
- safe-mode state;
- deterministic resource-constrained state.

Previous test assertions passed across the 3 configurations. Remaining work is preserving and visually inspecting the screenshot evidence. Screenshot persistence was moved to `/data/local/tmp/KINLINK-ui-proof` at exact head `c98aaf23...`.

## Exact next technical action

1. Poll Android run `35625701977`.
2. If FAIL: inspect exact preserved test/report artifact and correct only the proven failure.
3. If PASS: fetch candidate + responsive proof artifacts.
4. Inspect all 36 PNGs visually, not merely CI status.
5. Reject any clipping, overlap, one-character columns, absurd whitespace or broken controls.
6. Only if visual proof is clean: freeze exact candidate, canonical sign, stage Drive FIELD_CANDIDATE and read back hash.
7. Only then request one in-place phone update.
8. At tranche close: Gmail END -> provider ACK -> persist END_ACKNOWLEDGED -> short app reply only.

No phone installation is requested at this checkpoint.
