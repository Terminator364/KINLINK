# KINLINK build status

## Current field / development split
- Installed phone baseline: **0.7.3-dev / versionCode 11**
- Active development successor: **0.8.0-dev / versionCode 12**
- Branch: `dev/0.8.0-integrated-truth-and-control`
- Exact head: `5a45fb3ec5d4d82960b7687fdd75836e6770ccb4`
- No signing, Drive staging or phone installation requested.

## Full A+B+C
- macro capabilities: **80**
- B-depth: **B31-B100**
- conservative maturity: **46.0%**
- active wave: **W2 Context + Mobile Vault**
- no score inflation from generation/restart-infrastructure proof.

## K25-20 — B95 active-data generation semantics
- design-lint `35710089427`: **PASS**
- Android CI `35710089508`: **PASS**
- build artifact `10686770362`
- responsive UI proof artifact `10686325691`
- all Android workflow steps PASS.

### Proven behavior
- default-data and active-data generations are independent;
- active change invalidates old active-bound evidence;
- default-only change leaves active-bound evidence current;
- observation loss forces UNKNOWN;
- reappearance after unknown creates a new generation;
- no real subscription identifier reaches the core model;
- B94 privacy fences remain PASS.

## Next integrated block
**B96 — usage callbacks are advisory/process-live only**
- callback absence never proves zero usage;
- restart requires fresh reconciliation;
- model/process-kill tests first;
- no callback API activation or permission expansion yet;
- no signing/staging/install.
