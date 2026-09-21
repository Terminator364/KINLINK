# NEXT_UNCOMMITTED_ACTION

## True current state

### Phone
- Installed now: KINLINK 0.7.0 / versionCode 8.
- Keep it installed until the single successor gate below.
- 0.7.0 is usable but not canonically promotable.

### Canonical rollback
- KINLINK 0.6.0-rc3 / versionCode 7 remains the canonical Drive installer and rollback.
- Do not replace it until Stage B passes.

### Consolidated 0.7.1 Mobile Assist field candidate
- versionName: 0.7.1
- versionCode: 9
- exact functional source: `a15a784ab51ae048e37ae31c998092ad2cd3f03c`
- design-lint run `35594766801`: PASS
- Android candidate run `35594766821`: PASS
- artifact ID: `10636385308`
- artifact ZIP SHA-256: `07b3e0c5110bc866b0fce53b9ab1abb3784b72b8db2dda068d000ffe50921175`
- unsigned APK SHA-256: `f4b3f3c42d2f8d90a4ecfddac4ff580fb7afdce8e8bfb7ef9f39d373f5a68507`
- signed APK SHA-256: `0c2210acf29e18926634fc1d8b23b156cce153e1b536bb62b87615199dca6eae`
- signed size: 357323 bytes
- signer cert SHA-256: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- APK Signature Scheme v3: PASS
- Drive file ID: `1hixAB1tKY8G3u-ajD16PJ6kQ-lznNaV1`
- Drive readback SHA-256: exact MATCH / PASS

### Mobile Assist Level 1 included
- passive mobile low-capacity / congestion / suspension / weak-radio diagnosis;
- no automatic cellular HTTP/DNS/speedtest;
- bounded Android bandwidth-metric refresh on validated degraded cellular;
- 5-minute automatic cooldown;
- max 6 actions/hour;
- resource / Mobile Vault / safe-mode / weak-radio suppression;
- anti-repeat after ineffective outcomes;
- explicit user Android connectivity-panel fallback;
- passive mobile slow-link history in cockpit and diagnostics.

## Current gate

Machine-side work for this exact candidate is complete.

The next action is a **true human gate**:
1. install the exact Drive APK in-place over existing 0.7.0;
2. do not uninstall 0.7.0 first;
3. after install, collect versionCode 9:
   - core self-test PASS;
   - observer callback self-test PASS;
   - Wi-Fi -> validated cellular;
   - cellular -> Wi-Fi return;
   - >=30-minute resource qualification;
4. promote canonically only after Stage B PASS.

No additional micro-beta is allowed before this gate unless the current candidate is explicitly invalidated.

## Communication protocol
- Gmail start first.
- Persist start message ID.
- Coherent 25-minute tranche.
- Intermediate feedback does not erase end-mail obligation.
- Final technical checkpoint.
- Gmail end report first.
- Require end Gmail message ID.
- Persist tranche CLOSED.
- Only then app reply: check Gmail + Kinshasa date/time.
- APK never by Gmail.

## Resume
`KINLINKGO`
