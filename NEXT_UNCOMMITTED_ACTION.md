# NEXT_UNCOMMITTED_ACTION

## Field gate — PASS

Proven on target phone:
- KINLINK 0.6.0-rc3 installed;
- Wi-Fi → cellular handoff PASS;
- mobile Internet available;
- KINLINK observation-only on cellular;
- cellular → Wi-Fi handoff PASS;
- Wi-Fi Internet restored;
- original mobile-data-block symptom not reproduced.

## Post-field hardening now integrating

1. Suppress transient OFFLINE publication for 1.5 s after default-network loss so normal Android handoff does not flash a false outage.
2. Correct mobile-data wording:
   - counter is Android device-wide mobile TrafficStats delta observed by KINLINK;
   - it is not KINLINK app traffic;
   - it is not operator balance.
3. Keep these fixes in the next consolidated build; do not force a new install solely for cosmetic wording.
