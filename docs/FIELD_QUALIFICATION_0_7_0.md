# KINLINK 0.7.0 — Field Qualification Protocol

## Objective

Qualify exactly one stable-signed 0.7.0 field candidate before canonical promotion. The existing 0.6.0-rc3 Drive installer remains the rollback baseline until Stage B is complete.

## No micro-beta rule

- Do not uninstall RC3 to install the candidate.
- Do not install any superseded 0.7 artifact.
- Do not replace `KINLINK/INSTALLER/KINLINK_LATEST.apk` during field qualification.
- Only the final APK hash recorded in the 0.7 release evidence may be installed.

If Android refuses an in-place update, stop and preserve RC3. Never work around a signer/package mismatch by uninstalling the baseline.

## Automatic evidence after install

For versionCode 8, evidence is scoped to version-specific receipts so RC3 history cannot qualify 0.7.0:

- `SELF_TEST_CORE_V8`
- `SELF_TEST_OBSERVER_CALLBACK_V8`
- `HANDOFF_OUTCOME_MOBILE_VALIDATED_V8`
- `HANDOFF_CELLULAR_TO_WIFI_V8`
- `RUNTIME_RESOURCE_GATE_PASS_V8`
- terminal negative resource evidence: `RUNTIME_RESOURCE_GATE_BLOCKED_V8`

When all required positive evidence exists and no resource block exists, KINLINK writes:

`FIELD_CANDIDATE_QUALIFIED_V8`

If resource qualification is blocked, KINLINK writes:

`FIELD_CANDIDATE_BLOCKED_V8`

## Minimal target-phone sequence

1. Install the one final stable-signed 0.7.0 candidate **over RC3**.
2. Android `MY_PACKAGE_REPLACED` should request observer-service restart automatically.
3. Open KINLINK once to see version/status and to ensure normal UI access.
4. Use the phone normally.
5. For clean battery evidence, the phone should spend the qualification window off the charger.
   - If it was charging, unplugging automatically resets the resource baseline and starts a fresh bounded 30-minute window.
   - A plugged-in flat battery is INCONCLUSIVE, never PASS.
6. Produce one Wi-Fi -> validated cellular transition.
7. Produce one cellular -> Wi-Fi return.
8. No screenshot is required to determine the local state: the main UI and foreground notification expose QUALIFIED or BLOCKED.
9. Export the diagnostic only when evidence needs to be transferred out of the phone for the canonical promotion receipt.

## Stage B gate

Canonical promotion is allowed only when:
- MACHINE = PASS
- MIGRATION = PASS
- SIGNER_CONTINUITY = PASS
- FIELD_HANDOFF = PASS on versionCode 8 evidence
- RESOURCE_QUALIFICATION = PASS on versionCode 8 evidence

Until then, RC3 remains canonical.
