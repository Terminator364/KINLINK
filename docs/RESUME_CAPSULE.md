# KINLINK — deterministic resume capsule

`.project-memory/RESUME_CAPSULE.json` is the first durable object a new KINLINK conversation must load.

It has two jobs:

1. **Detect partial synchronization.** Core A+B+C, memory, communication, delivery and visual-policy files are pinned by their Git blob IDs. If any canonical input changes without refreshing the capsule, `design-lint` fails.
2. **Force fresh reads for volatile state.** The capsule deliberately does not freeze `ACTIVE_TRANCHE`, the communication ledger, project state, chronicle or `NEXT_UNCOMMITTED_ACTION`; a takeover must read those files fresh.

This prevents two opposite failure modes:
- stale summaries silently overriding newer canonical files;
- a frozen handoff capsule pretending that a live tranche/run still has an old state.

## Cold-start order

1. verify capsule blob set;
2. read fresh communication state;
3. reconcile any unclosed tranche;
4. reload A+B+C + scorecard;
5. reload integrated W0-W6 plan + dead-end registry;
6. read fresh engineering state + next action;
7. send Gmail START only if substantive work will continue;
8. persist START provider ACK;
9. work.

A failure at steps 1–6 is a **HOLD**, not permission to guess from conversation memory.
