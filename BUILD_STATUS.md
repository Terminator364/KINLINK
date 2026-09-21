# KINLINK build status

## Field baseline installed
- KINLINK **0.7.3-dev / versionCode 11**
- installed and usable as the current field baseline
- **not near-final** after target-device visual/truthfulness counter-audit
- keep installed while the integrated successor is developed internally

## Full product scope
- A+B+C macro capabilities: **80**
- conservative maturity: **44.8%**
- B-depth: **B31-B90** / 60 concrete subrequirements
- integrated program: **W0-W6**
- active wave: **W1 — Platform diagnostics + capability discovery**
- human installation is not the unit of progress; no micro-beta chain

## Last fully visual-audited 0.8 head
- head `b54020291f866042103ad0f63cae3645977638d1`
- design-lint `35656916055`: PASS
- Android `35656916207`: PASS
- rendered/manual proof: **45/45 inspected**
- verdict: UI/truth PASS
- no field install authorized

## Release-provenance incident found after CI
Head `62a910a698286896af90d22551366f728ee0c4e7` passed CI, but artifact readback found:

- APK identity itself: 0.8.0-dev / versionCode 12
- generated `KINLINK-build-manifest.json`: literal `\\1` for version and versionCode

Cause: shell/sed backreference escaping produced the same bad value that the workflow later compared, so the self-check was not independent enough.

This blocks signing/staging despite green CI.

## Active exact 0.8 successor
- branch: `dev/0.8.0-integrated-truth-and-control`
- head: `e633db6f6441f563256deb04890bbc28267d08b8`
- change: deterministic Python parser reads versionName/versionCode from Gradle, writes JSON, then read-backs parsed JSON
- design-lint `35658883138`: **PASS**
- Android `35658883177`: **IN PROGRESS**
- no signing, Drive staging or phone install until exact-head provenance readback passes

## Continuity
- deterministic capsule: `.project-memory/RESUME_CAPSULE.json`
- capsule verifier: `tools/release/verify_resume_capsule.py`
- cold takeover verifier: `tools/release/verify_cold_takeover.py`
- communication protocol: v5
- one watchdog only: **KINLINK Comms Watchdog**
