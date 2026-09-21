# KINLINK 0.7.2 — final-like field-candidate counter-audit

Status: MACHINE PASS / SIGNED / DRIVE READBACK PASS / FIELD PENDING
Exact code head: `cc48c1dea1a151af25edc1942bb4efa983c5ff13`
Immutable source branch: `release/0.7.2-field-candidate`

## Evidence chain

- design-lint run `35610708971`: PASS
- design-lint run `35610716886`: PASS
- Android candidate run `35610708876`: PASS
- artifact ID `10643704115`
- artifact ZIP SHA-256:
  `ad2fee91b3d51fa5c2528767dbe004d92366ee8ae46bb9450a35f11a86f7f18b`
- unsigned APK SHA-256:
  `d961ea42c09a6509a228f4bfe9b55b197598efaea2f942ef649e8c8983925779`
- signed APK SHA-256:
  `a67537e814f81230526ff5de211bdfcd6d810831fcaed75fdc1252578e5b364b`
- canonical signer:
  `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`
- Drive readback SHA-256: exact match.

## Counter-audit questions

### 1. Does 0.7.2 claim more than it can prove?

No machine-side overclaim was retained.

`requestBandwidthUpdate()` is described as metric refresh/observation improvement. A later better passive score is called correlated evidence, not proof that KINLINK increased carrier throughput.

Strong causal traffic handling remains separately gated.

### 2. Is “continuous improvement” just a button press?

No.

The 0.7.2 loop now has:
- pre-action baseline;
- bounded post-action observations;
- meaningful passive score delta;
- sustained-confirmation interval;
- early relapse;
- later relapse after sustained improvement;
- bounded re-evaluation after normal gates;
- anti-repeat after repeated no-benefit evidence.

No repeating HTTP/DNS/speedtest loop was introduced.

### 3. Can follow-up create heat/battery churn?

Controls:
- no automatic cellular test traffic;
- max three one-shot local framework samples after an accepted action;
- one best-effort later relapse check;
- delayed Handler callbacks do not request exact alarms or wake a sleeping device;
- callbacks are cancelled/fenced by generation when a newer action starts;
- resource guard aborts evidence work under constrained device state;
- healthy validated cellular returns before SQLite/resource arbitration;
- active assistance pauses from Android moderate thermal pressure;
- normal Mobile Assist cooldown/hourly cap remains.

Residual field gate:
- target-phone v10 resource evidence is still required.

### 4. Can old evidence contaminate a new action?

Machine-side controls:
- atomic/generation fencing;
- new assist action invalidates older evidence callbacks;
- manual proof uses a pre-action baseline;
- baseline uses action-time monotonic clock;
- late relapse is treated as a later state of its earlier sustained event, not a second independent action.

### 5. Can handoff make mobile evidence stale?

Evidence becomes INCONCLUSIVE or is discarded when transport/validation no longer matches the cellular proof window.

Existing default-network callback two-phase fencing remains inherited from 0.7.1.

### 6. Is the UI materially different from 0.7.1?

Yes, machine-side layout changes are substantial:
- status/action/proof hierarchy;
- only transport-relevant primary assist action;
- smaller paired secondary controls;
- technical details collapsed by default;
- passive quality display;
- proof summary placed in the continuous-care card;
- large-font/wrap-content fixes.

Residual field gate:
- target-phone screenshot/ergonomic validation on v10.

### 7. Is 0.7.2 a micro-beta?

No.

It is a consolidated successor to the installed 0.7.1, containing UX, evidence, relapse, resource-path and truthfulness changes as one frozen candidate.

No further micro build should replace it unless a blocking defect is proven.

## Residual risks that remain honest

### FIELD-01 — Android passive bandwidth estimates are not measured throughput
Mitigation:
- UI says passive;
- no speedtest implication;
- causal claim forbidden.

### FIELD-02 — resource cost must be measured on the target phone
Mitigation:
- existing >=30-minute resource gate;
- no canonical promotion before PASS.

### FIELD-03 — real unstable-network benefit is not machine-provable
Mitigation:
- versionCode 10 field evidence;
- handoff evidence;
- continuous Mobile Assist outcome receipts;
- incident markers/diagnostic export.

### FIELD-04 — strong stabilizer is not production-qualified
Mitigation:
- remains separate experiment gate;
- no unverified VpnService inserted into 0.7.2.

## Machine-side verdict

No known P0/P1 machine blocker remains on exact frozen head `cc48c1d...`.

The next defensible step is one in-place target-phone install of the exact Drive-readback 0.7.2 APK, followed by Stage B field qualification.

Canonical INSTALLER must remain 0.6.0-rc3 until Stage B PASS.
