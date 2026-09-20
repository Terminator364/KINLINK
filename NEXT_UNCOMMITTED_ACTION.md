# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

- M1 APK was built, installed and used on the target phone.
- Commit `75337b7ca5119683f16c456e713da0fcf22f27ac` added the explicit Wi-Fi-only Doctor.
- M1 remains safe but mostly diagnostic; it does not materially steer Android connectivity.

## Current candidate

M2 adds the first bounded active optimizer:
1. explicit Wi-Fi-only micro-probe;
2. report observed connectivity back to Android for framework re-evaluation;
3. request refreshed bandwidth metrics;
4. refuse all active optimization on cellular;
5. preserve fail-open and no-speedtest guarantees.

## Next action

Wait only for the GitHub Actions M2 result. If tests and APK build pass:
- capture the APK SHA-256 and manifest;
- promote the single installer candidate;
- then request one focused phone install/use check.
Do not send another diagnostic-only APK.
