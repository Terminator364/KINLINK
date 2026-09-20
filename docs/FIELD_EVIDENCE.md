# KINLINK field evidence ledger

## 2026-09-20 — M2 contradiction discovered on target phone

Observed simultaneously:
- cockpit state: WIFI_HEALTHY
- LAN: available
- Android-derived Internet state: healthy/validated
- explicit optimizer result: Réévaluation Wi-Fi demandée / Le Wi-Fi répond mal

Root cause:
- M2 allowed one failed external 204 endpoint probe to choose the negative optimizer action even when Android already reported NET_CAPABILITY_VALIDATED.

Classification:
- logic/evidence-arbitration defect;
- false-negative diagnosis;
- not proof of Wi-Fi failure.

Permanent regression rule:
- Android VALIDATED must not be downgraded by a failed single external probe.
- external endpoints are supporting evidence only.
- if a probe fails while Android remains validated, classify the probe as inconclusive/endpoint-specific and preserve the healthy Internet state.

M3 adds a bounded fallback endpoint plus regression tests for this exact failure mechanism.
