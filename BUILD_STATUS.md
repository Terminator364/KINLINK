# KINLINK build status

## Current field / development split
- Installed phone baseline: **0.7.3-dev / versionCode 11**
- Active development successor: **0.8.0-dev / versionCode 12**
- Branch: `dev/0.8.0-integrated-truth-and-control`
- Exact head: `e341c3e2cb95649cb5a441bd3aca7aacbf835b52`
- No signing, Drive staging or phone installation requested.

## Full A+B+C
- macro capabilities: **80**
- B-depth: **B31-B100**
- conservative maturity: **46.0%**
- active wave: **W2 Context + Mobile Vault**
- no score inflation from infrastructure/privacy-boundary proof.

## K25-19 — B94 Subscription ID privacy boundary
- design-lint `35707268025`: **PASS**
- Android CI `35707267993`: **PASS**
- build artifact `10685735166`
- responsive UI proof artifact `10684824257`
- all Android workflow steps PASS.

### Proven boundary
- aggregate-only attribution is the default;
- per-subscription association requires independent need + permission gates;
- READ_PHONE_STATE / READ_PHONE_NUMBERS absent and forbidden;
- sensitive/non-resettable identity getter APIs forbidden;
- SubscriptionManager/createForSubscriptionId deliberately fenced for now;
- no subscription identity in diagnostics/export;
- no non-resettable identifier use.

## Next integrated block
**B95 — active-data subscription semantics**
- model default-data separately from active-data;
- generation/staleness invalidation first, with pure tests;
- no telephony API or permission expansion yet;
- aggregate/UNKNOWN fallback;
- no signing/staging/install.
