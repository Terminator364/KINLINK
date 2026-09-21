# NEXT_UNCOMMITTED_ACTION

## Installed target-phone build

- Installed now: KINLINK 0.7.1 / versionCode 9.
- Keep it installed until the single in-place 0.7.2 update below.
- 0.7.1 is usable but superseded before canonical promotion.
- Canonical rollback remains 0.6.0-rc3 / versionCode 7.

## Consolidated 0.7.2 final-like field candidate

Source branch:
`dev/0.7.2-continuous-improvement`

Exact frozen source:
`cc48c1dea1a151af25edc1942bb4efa983c5ff13`

Machine proof:
- design-lint `35610708971`: PASS
- design-lint `35610716886`: PASS
- Android candidate `35610708876`: PASS
- unit tests: PASS
- Android lint: PASS
- fail-open API fence: PASS
- manifest safety fence: PASS
- background wakeup fence: PASS
- package/version/unsigned candidate fence: PASS

Artifact:
- artifact ID: `10643704115`
- ZIP SHA-256:
  `ad2fee91b3d51fa5c2528767dbe004d92366ee8ae46bb9450a35f11a86f7f18b`
- unsigned APK SHA-256:
  `d961ea42c09a6509a228f4bfe9b55b197598efaea2f942ef649e8c8983925779`

Stable signed field APK:
- SHA-256:
  `a67537e814f81230526ff5de211bdfcd6d810831fcaed75fdc1252578e5b364b`
- size: 369611 bytes
- APK Signature Scheme v3: PASS
- signers: 1
- signer cert SHA-256:
  `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`

Drive:
- file: `KINLINK-0.7.2-FINAL-FIELD-CANDIDATE.apk`
- file ID: `1pBf93qjb9vE3T3BIW7Jg54DuSIch3wSU`
- Drive readback SHA-256:
  `a67537e814f81230526ff5de211bdfcd6d810831fcaed75fdc1252578e5b364b`
- readback: PASS

## What materially changed

0.7.2 is not a cosmetic micro-beta.

It consolidates:
- compact status/action/proof cockpit from target-phone screenshot audit;
- contextual primary action: Wi-Fi OR Mobile, not both;
- technical details collapsed by default;
- passive quality index without speedtest;
- truthful Mobile Assist wording;
- pre-action baseline for manual Mobile Assist;
- sustained benefit confirmation;
- transient relapse;
- post-sustained relapse;
- bounded one-shot framework samples;
- anti-repeat evidence;
- healthy mobile fast path to reduce DB/resource overhead;
- earlier thermal protection;
- large-font cockpit corrections;
- privacy/fail-open/mobile-data invariants from 0.7.1.

## Truth boundary

Mobile Assist Level 1 does not claim carrier-throughput causality.
`requestBandwidthUpdate` improves/refreshes Android's link metrics; later quality change is recorded as correlated evidence.

A strong VpnService/TUN data plane remains a separate experiment gate and cannot be enabled until measurable benefit + RAM/battery/latency/DNS/watchdog/rollback evidence passes.

## Current true human gate

One in-place target-phone update to exact 0.7.2 is now justified.

Rules:
1. do not uninstall 0.7.1 first;
2. install the exact Drive APK above;
3. after install collect versionCode 10 self-test/handoff/resource/continuous-evidence receipts;
4. do not replace canonical 0.6.0-rc3 until Stage B passes.

No further micro-beta is allowed before this field gate unless 0.7.2 is explicitly invalidated by a newly proven blocking defect.

## Communication v2

Normal closure is primary-owned:
- 22 min work;
- 3 min normal close reserve;
- END replies to START in the same Gmail thread;
- delivery key prevents duplicate END;
- backup cannot preempt normal close;
- backup searches Gmail before send and uses normal FIN wording;
- app reply only after persisted provider END acknowledgement.

## Resume

`KINLINKGO`
