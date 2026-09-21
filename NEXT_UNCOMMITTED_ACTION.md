# NEXT_UNCOMMITTED_ACTION

## Stable field baseline
0.6.0-rc3 / versionCode 7 remains installed and Drive-canonical. Do not replace it yet.

## Frozen candidate line
0.7.0 / versionCode 8.

## Latest full machine PASS
`a00ed98ab269a851f38fb33cfda8bb77a624e502`.

That head proved the release-like unsigned candidate variant builds successfully.

## Current head under qualification
`9a4a0c5c1dd9e88eb2dd03983281b8115159a6ab`.

- design-lint: PASS
- Android candidate CI: IN_PROGRESS at checkpoint time

## Hardening added in this tranche
1. diagnostic summary now carries previously omitted 24h slow-link, incident and manual-diagnosis evidence;
2. runtime resource verdicts are exported;
3. churn limit is normalized by session duration;
4. one-shot >=30-minute resource checkpoint is independent from network callbacks, with shutdown fallback;
5. background repeating-wakeup APIs are fenced in CI;
6. release readiness is two-stage: Stage A field-candidate, Stage B canonical promotion;
7. canonical signer contract is pinned and CI-verified;
8. candidate build is release-like and unsigned in public CI;
9. private signing material is statically forbidden from the public repository;
10. candidate identity is verified as package com.terminator364.kinlink, versionCode 8, versionName 0.7.0, and unsigned;
11. offline signer is pinned to the exact CI input hash and canonical signer fingerprint.

## Next durable action
1. read Android CI for `9a4a0c5c1dd9e88eb2dd03983281b8115159a6ab`;
2. if PASS, download the exact `KINLINK-0.7.0-ci-build` artifact and verify its hash/manifest;
3. stable-sign exactly that candidate with the canonical signer in the private signing path;
4. do not replace RC3 yet;
5. install at most one consolidated signed 0.7.0 field candidate;
6. collect FIELD_HANDOFF + >=30-minute RESOURCE_QUALIFICATION;
7. canonically promote only after all five gates PASS.
