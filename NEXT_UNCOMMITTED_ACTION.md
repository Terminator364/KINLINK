# NEXT_UNCOMMITTED_ACTION

## Stable field baseline
0.6.0-rc3 / versionCode 7 remains installed and Drive-canonical. Do not replace it with a DEV build.

## Active development line
0.7.0-dev / versionCode 8.

## Latest full machine PASS
`1b404fa59aba02746f68291f21f8b8a5e533544f`.

## Current code head under qualification
`c8999e902e92af51e34c95349073135df4de6ecc`.

## Newly hardened
1. v1 -> v5 telemetry migration now preserves the exact historical schema at every step;
2. migration execution errors are no longer silently swallowed;
3. regression tests enforce the historical schema boundaries;
4. signed-candidate promotion is centralized in ReleaseReadinessPolicy;
5. MACHINE, MIGRATION, SIGNER_CONTINUITY, FIELD_HANDOFF and RESOURCE_QUALIFICATION are all independently mandatory.

## Current gate
- Android CI: IN_PROGRESS at checkpoint time.
- design-lint: queued at checkpoint time.
- RC3 field baseline remains untouched.

## Next durable action
1. read CI for `c8999e902e92af51e34c95349073135df4de6ecc`;
2. if both workflows PASS, record it as latest full machine PASS;
3. continue release-readiness evidence, especially target-device >=30-minute resource qualification;
4. do not sign/promote/install 0.7.0-dev until all readiness gates are proven.
