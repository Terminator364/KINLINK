# KINLINK build status

## Canonical rollback

- KINLINK 0.6.0-rc3 / versionCode 7
- Drive INSTALLER remains unchanged.
- It remains canonical until a newer Stage B field gate passes.

## Installed phone

- KINLINK 0.7.1 / versionCode 9
- installed and usable
- superseded before canonical promotion by the consolidated 0.7.2 field candidate

## KINLINK 0.7.2 final-like field candidate

Exact source:
`cc48c1dea1a151af25edc1942bb4efa983c5ff13`

Branch:
`dev/0.7.2-continuous-improvement`

CI:
- design-lint `35610708971`: PASS
- design-lint `35610716886`: PASS
- Android candidate `35610708876`: PASS
- unit tests: PASS
- Android lint: PASS
- API fail-open fence: PASS
- manifest safety fence: PASS
- background wakeup fence: PASS
- candidate identity/unsigned proof: PASS

Artifact:
- ID `10643704115`
- ZIP SHA-256 `ad2fee91b3d51fa5c2528767dbe004d92366ee8ae46bb9450a35f11a86f7f18b`
- unsigned APK SHA-256 `d961ea42c09a6509a228f4bfe9b55b197598efaea2f942ef649e8c8983925779`

Signed:
- SHA-256 `a67537e814f81230526ff5de211bdfcd6d810831fcaed75fdc1252578e5b364b`
- size 369611 bytes
- APK Signature Scheme v3 PASS
- 1 signer
- canonical cert SHA-256 `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`

Drive:
- file ID `1pBf93qjb9vE3T3BIW7Jg54DuSIch3wSU`
- readback SHA-256 exact match
- readback PASS

## Major final-like delta

### Cockpit
- compact status/action/proof structure;
- only relevant transport action visible;
- secondary controls compressed;
- technical content progressively disclosed;
- large-font layout hardened.

### Continuous care
- no instant “success” claim;
- meaningful passive-score delta required;
- sustained confirmation window;
- transient relapse detected;
- later relapse detected;
- bounded re-evaluation after normal safeguards;
- no repeating speedtest/polling storm.

### Resources
- healthy mobile callbacks avoid unnecessary SQLite/resource work;
- active work suppressed under budget/safe-mode/weak-radio/resource gates;
- thermal protection begins at Android moderate thermal pressure;
- evidence sampling is bounded.

### Truthfulness
- Android metric refresh is not called throughput acceleration;
- strong causal stabilizer remains gated.

## Release gates

Stage A:
- MACHINE: PASS
- MIGRATION: PASS
- SIGNER_CONTINUITY: PASS
- DRIVE_READBACK: PASS

Stage B:
- FIELD_HANDOFF v10: PENDING
- RESOURCE_QUALIFICATION v10: PENDING
- CONTINUOUS_EVIDENCE_FIELD: PENDING

## Current gate

One in-place 0.7.2 target-phone update is the next required field step.
Do not uninstall 0.7.1 first.
Do not replace canonical INSTALLER yet.
