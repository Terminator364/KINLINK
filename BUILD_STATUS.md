# KINLINK build status

## Current verdict

### Canonical rollback
- 0.6.0-rc3 / versionCode 7
- Drive INSTALLER remains unchanged
- canonical until Stage B promotion proof

### Installed phone
- 0.7.0 / versionCode 8
- usable
- canonical promotion invalidated by post-install counter-audit
- keep installed until the single 0.7.1 update gate

### 0.7.1 Mobile Assist field candidate

Exact functional source:
`a15a784ab51ae048e37ae31c998092ad2cd3f03c`

Machine proof:
- design-lint run `35594766801`: PASS
- Android candidate run `35594766821`: PASS
- unit tests: PASS
- Android lint: PASS
- fail-open API fence: PASS
- manifest safety fence: PASS
- background wakeup fence: PASS
- candidate package/version identity: PASS
- unsigned candidate proof: PASS

Artifact:
- artifact ID `10636385308`
- artifact ZIP SHA-256 `07b3e0c5110bc866b0fce53b9ab1abb3784b72b8db2dda068d000ffe50921175`
- unsigned APK SHA-256 `f4b3f3c42d2f8d90a4ecfddac4ff580fb7afdce8e8bfb7ef9f39d373f5a68507`
- signed APK SHA-256 `0c2210acf29e18926634fc1d8b23b156cce153e1b536bb62b87615199dca6eae`
- signed size 357323 bytes
- APK Signature Scheme v3: PASS
- signers: 1
- signer cert SHA-256 `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`

Drive:
- FIELD_CANDIDATE file ID `1hixAB1tKY8G3u-ajD16PJ6kQ-lznNaV1`
- Drive readback SHA-256 exactly matches signed local APK
- Drive readback: PASS

## Major consolidated capability

0.7.1 includes:
- Wi-Fi fail-open recovery hardening;
- exact version-scoped qualification receipts;
- runtime resource qualification;
- bounded probe workers/deadlines;
- stale callback/handoff race fences;
- structured privacy-safe diagnostic ZIP;
- Mobile Assist Level 1:
  - passive cellular low-capacity/congestion/suspension/weak-radio diagnosis;
  - zero automatic cellular HTTP/DNS/speedtest;
  - bounded Android metric refresh on validated degraded cellular;
  - 5-minute cooldown + 6/hour cap;
  - resource / budget / safe-mode / weak-radio gates;
  - anti-repeat after ineffective outcomes;
  - explicit Android connectivity-panel fallback;
  - passive mobile slow-link history and diagnostics.

## Release gates

Stage A:
- MACHINE: PASS
- MIGRATION: PASS
- SIGNER_CONTINUITY: PASS
- Drive field-candidate readback: PASS

Stage B:
- FIELD_HANDOFF: PENDING target phone
- RESOURCE_QUALIFICATION: PENDING target phone

## Next gate

One in-place update to exact 0.7.1 field candidate.
No uninstall first.
No canonical Drive promotion yet.
No micro-beta chain.
