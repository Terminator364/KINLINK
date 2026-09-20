# FIELD_EVIDENCE_0_6_0_RC3

## 2026-09-20 — Bidirectional manual handoff

Target phone:
- KINLINK version visible in UI: 0.6.0-rc3
- Test performed at home; no physical movement required.

### Wi-Fi → cellular
Observed:
- user disabled Wi-Fi;
- cellular / 4G+ became active;
- UI showed "Données mobiles disponibles";
- UI showed "Internet · Disponible";
- UI showed "Données mobiles · Android contrôle · KINLINK observe seulement";
- original mobile-block symptom was not reproduced.

Verdict: PASS.

### Cellular → Wi-Fi
Observed:
- user re-enabled Wi-Fi;
- one transient no-network/offline UI frame appeared during Android handoff;
- final state became "Wi-Fi prêt";
- final transport became Wi-Fi;
- final Internet state became "Disponible";
- mobile data returned to protected/non-owned state.

Verdict: PASS.

### Field gate
- BIDIRECTIONAL_HANDOFF: PASS
- ORIGINAL_MOBILE_BLOCK_SYMPTOM: NOT_REPRODUCED
- PHYSICAL_WALK_OUT_OF_WIFI_RANGE: NOT_REQUIRED_FOR_THIS_GATE
- TRANSIENT_OFFLINE_FLASH: OBSERVED; queued/fixed by delayed loss publication
- MOBILE_COUNTER_LABEL: misleading; queued/fixed to explicitly describe Android device-wide counter observation
