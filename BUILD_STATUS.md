# KINLINK build status

## Stable field baseline

M2 is installed and field-visible. Stable signing continuity is established.

## Field defect converted into M3 regression

The target phone exposed a contradiction:
- Android/KINLINK truth engine: WIFI_HEALTHY
- M2 explicit optimizer: negative revalidation because one external endpoint failed

This is now treated as a P0 evidence-arbitration defect, not as proof of poor Wi-Fi.

## M3 candidate

- Android VALIDATED authority preserved
- captive portal has priority
- fallback micro-probe endpoint
- negative revalidation only on coherent negative evidence
- richer technical evidence
- unit regression tests
- no automatic mobile-data probe

M3 must pass CI, stable-sign promotion and update-in-place verification before field installation.
