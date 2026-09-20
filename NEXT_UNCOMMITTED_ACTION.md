# NEXT_UNCOMMITTED_ACTION

## Field gate status

PASS:
- KINLINK 0.6.0-rc3 installed.
- Manual Wi-Fi OFF → cellular handoff.
- Cellular transport active.
- Internet available.
- KINLINK observation-only on cellular.
- Original mobile-data-block symptom not reproduced.

PENDING:
1. Re-enable Wi-Fi.
2. Confirm Android returns to Wi-Fi normally.
3. Confirm KINLINK reports Wi-Fi ready/usable again.
4. If successful, close the bidirectional handoff field gate.

## UI issue queued for next consolidated build

The label "Suivi KINLINK · X MiB" is semantically wrong.
The tracker observes device-wide mobile TrafficStats delta, not KINLINK's own mobile traffic.
Rename it to explicitly say device mobile usage observed by KINLINK.
Do not issue a new APK solely for this wording correction.
