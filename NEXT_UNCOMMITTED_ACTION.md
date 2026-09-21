# NEXT_UNCOMMITTED_ACTION

## Current user/device truth
- Phone currently runs KINLINK 0.7.0 / versionCode 8.
- Installed signed hash: `cea3468340a8f81dc38cc1ba09abb68e8f9ccc1634e223f275b0e92b083ef0bc`.
- 0.7.0 may remain installed and usable.
- Its canonical promotion was invalidated by post-install counter-audit findings.
- **Do not ask the user to install another APK yet.**

## Canonical rollback/delivery truth
- Drive `KINLINK/INSTALLER/KINLINK_LATEST.apk` remains the canonical 0.6.0-rc3 rollback/stable artifact.
- Future noncanonical successor delivery belongs in `KINLINK/FIELD_CANDIDATE`.
- Never distribute APKs by Gmail.

## Active consolidated successor
- Version: 0.7.1
- versionCode: 9
- Purpose: close the full post-install counter-audit in one successor; no micro-beta chain.
- Last full exact-head machine PASS before current hardening: `a8e4b1a6510ee3bf06d1143253b08437e745cab4`
  - Android run 35575247146 PASS
  - design-lint run 35575247175 PASS

## Counter-audit repair scope
- CA-001 hard recovery deadline — repaired
- CA-002 battery quantization false-block risk — repaired
- CA-003 bounded resource retry — repaired
- CA-004 historical BLOCKED UI truthfulness — repaired
- CA-005 irreplaceable receipt retention — repaired
- CA-006 cross-version prefix receipt matching — repaired, exact SQL equality
- CA-007 historical QUALIFIED forcing stale PASS — repaired, live verdict is revocable
- CA-008 DNS/socket wall-clock deadline proof — repaired with outer timeout/disconnect
- CA-009 stale old-default callbacks contaminating handoff evidence — repaired

Additional hardening:
- cross-policy critical scenario matrix;
- candidate-contract static counter-audit;
- requirements traceability matrix separating current proof from later gated architecture;
- mandatory 25-minute Gmail-first/Gmail-last protocol persisted.

## Next durable engineering action
1. obtain full Android CI + design-lint PASS on the newest exact head containing CA-006..CA-009;
2. download the exact 0.7.1 unsigned release-like CI artifact;
3. verify artifact ZIP digest, APK SHA-256, package/version and build-manifest commit;
4. perform a second counter-audit against the exact artifact/source head;
5. stable-sign exactly that artifact with the canonical signer certificate;
6. verify signed APK + signer fingerprint + byte/readback evidence;
7. place exactly one successor in Drive `KINLINK/FIELD_CANDIDATE`;
8. only then open the next real human gate: one in-place 0.7.1 install;
9. target phone must then produce v9 handoff + resource evidence before any canonical Drive promotion.

## Communication protocol
- Gmail start first.
- Work one coherent 25-minute tranche.
- Intermediate feedback does not interrupt/restart the tranche.
- Stop early only for a true human gate objectively required to continue.
- Gmail complete end report before any app response.
- App response only: check Gmail + Kinshasa date/time.
- Gmail label: `KINLINK`.

## Resume command
`KINLINKGO`
