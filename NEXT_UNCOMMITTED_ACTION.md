# NEXT_UNCOMMITTED_ACTION

## Proven target-phone field gate
PASS:
- Wi-Fi → cellular
- cellular Internet available
- KINLINK observation-only on cellular
- cellular → Wi-Fi
- Wi-Fi Internet restored
- original mobile-block symptom not reproduced

## Current machine batch
Validate in CI:
1. handoff UI settle window;
2. truthful mobile counter wording;
3. passive bandwidth truth propagation;
4. passive quality classification;
5. slow-but-valid Wi-Fi Autopilot behavior;
6. bounded manual probe responsiveness classification;
7. low-noise quality-tier telemetry.

## Delivery rule
Keep the installed signed RC3 as field baseline. Do not ask for another install until the next build contains a materially larger set of improvements.


## Version identity fence
- Installed/promoted field baseline remains immutable: 0.6.0-rc3 / versionCode 7.
- All post-field development now builds as 0.7.0-dev / versionCode 8.
- Do not overwrite the canonical Drive RC3 with a DEV artifact.
- Promote only after a future consolidated release gate renames the DEV line to a release candidate.
