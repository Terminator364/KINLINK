# FIELD_EVIDENCE_0_6_0_RC3

## 2026-09-20 — Manual Wi-Fi → cellular handoff

Target phone:
- KINLINK version visible in UI: 0.6.0-rc3
- Test method: user disabled Wi-Fi at home and enabled/used mobile data; no physical movement required.

Observed screenshots:
- active transport: cellular / 4G+
- KINLINK UI: "Données mobiles disponibles"
- KINLINK UI: "Internet · Disponible"
- KINLINK UI: "Données mobiles · Android contrôle · KINLINK observe seulement"
- Autopilot: prudent/passive on cellular
- no automatic mobile test shown

Verdict:
- MANUAL_WIFI_OFF_TO_CELLULAR: PASS
- ORIGINAL_MOBILE_BLOCK_SYMPTOM: NOT_REPRODUCED in this manual handoff
- PHYSICAL_LOSS_OF_WIFI_RANGE: NOT_TESTED
- RETURN_CELLULAR_TO_WIFI: PENDING

Important UI finding:
- "Suivi KINLINK · 2598.8 MiB" is misleading wording.
- MobileBudgetTracker uses Android device-wide mobile TrafficStats deltas; the value is not KINLINK app traffic.
- Fix wording in next consolidated product build; do not force another install solely for this cosmetic/semantic defect.
