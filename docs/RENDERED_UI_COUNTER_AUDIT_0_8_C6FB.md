# KINLINK 0.8 rendered UI counter-audit — c6fb86e

Source: GitHub Actions run **35652449928**  
Exact head: `c6fb86ec8ef075f7e0cbdfd9cf0f04b8954088f1`  
Artifact: `KINLINK-0.8.0-responsive-ui-proof`  
Rendered images inspected: **45/45** across narrow-normal, medium-large and phone-xlfont.

## Machine gate

PASS:
- unit tests;
- Android lint;
- package/version identity;
- safety fences;
- rendered responsive assertions;
- artifact generation.

## Manual visual counter-audit

### Layout / responsiveness
PASS:
- no one-character button columns;
- no clipped primary buttons;
- no overlapping controls;
- Prudent / Stable / Max fit across all three profiles;
- mobile-data dialog remains usable at XL font;
- technical-details-start now shows the actual details body instead of the top of the page;
- technical-details-bottom reaches the lower diagnostic/export region;
- offline, safe mode and resource-protection states remain legible.

### Truthfulness
PASS relative to the 0.7.3 field defect:
- no user-facing 100/100 or 97/100 quality claim;
- Android bandwidth is not presented as measured user throughput;
- before/now/delta remain hidden without confirmed sustained benefit;
- “aucun gain causal confirmé” is explicit;
- Wi-Fi healthy state says access is available but quality is not measured.

### Non-blocking semantic findings
1. Primary 24 h history still used the term “stall(s) Android”, which is too technical for the normal cockpit.
2. Generic control title “Qualité sous surveillance” also appeared for OFFLINE; readable, but semantically weaker than an explicit offline title.
3. Platform capability/readiness (API 37 LAN permission, Data Saver, Usage Access, future VPN route support) was absent from technical details, making future feature gating less observable.

These findings were addressed on successor commit:
`b54020291f866042103ad0f63cae3645977638d1`

That successor requires its own fresh exact-head CI/render proof before promotion.

## Verdict

`c6fb86e`: **PASS_LAYOUT_AND_TRUTH_WITH_NONBLOCKING_SEMANTIC_FINDINGS**.

It is evidence for the 0.8 engineering line, not a field-release authorization. No signing/Drive/install gate is opened by this audit.
