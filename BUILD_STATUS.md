# KINLINK build status

## Current field / development split
- Installed phone baseline: **0.7.3-dev / versionCode 11**
- Active development successor: **0.8.0-dev / versionCode 12**
- Branch: `dev/0.8.0-integrated-truth-and-control`
- Exact head: `78b03442ab421ff5eb7f597488d40722fc9ab3cc`
- No signing, Drive staging or phone installation requested.

## Full A+B+C
- macro capabilities: **80**
- B-depth: **B31-B100**
- conservative maturity: **46.0%**
- active wave: **W2 Context + Mobile Vault**
- no score inflation from infrastructure-only proof.

## K25-18 — B92 + B93
- design-lint `35681033427`: **PASS**
- Android CI `35681033393`: **PASS**
- build artifact `10674639294`
- responsive UI proof artifact `10674619440`
- all Android workflow steps PASS: safety fences, unit tests, lint, unsigned candidate identity, responsive matrix, provenance and uploads.

### B92 — NetworkStats latency budget
- single bounded NetworkStats worker;
- single in-flight lease/token;
- **4 000 ms** deadline;
- timeout => cancel + session fail-open disable;
- stale timeout cannot disable newer query;
- main-thread query still rejected;
- query fenced out of `KinlinkObserverService` and `NetworkObserver`;
- cockpit/recovery remain independent.

### B93 — aggregate mobile is not per-plan truth
- NetworkStats remains `DEVICE_MOBILE_AGGREGATE`;
- explicit two-plan negative fixture => `HOLD_UNATTRIBUTED`;
- usage = null;
- both plan assessments remain `UNKNOWN`;
- no `PLAN_EXACT` promotion.

## Counter-audit
An intermediate design-lint falsely treated the lightweight `hasUsageAccess()` reader construction as a NetworkStats query. The contract was corrected to fence the actual `.query(request)` call. Final design-lint and Android CI are green.

## Next integrated block
**B94 — Subscription ID privacy boundary**
- negative CI fence against non-resettable telephony identifiers;
- no READ_PHONE_STATE/per-subscription activation without separately justified need;
- permission denial/unavailability remains aggregate/UNKNOWN;
- no signing/staging/install.
