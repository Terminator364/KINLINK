# NEXT_UNCOMMITTED_ACTION

## Proven state

Stable rollback installer: **0.4.0-rc1**.

Development head `7feaec6975638fe224614757e16977ff91302917` passed CI in run `35532459908`.

The field contradiction shown by the previous phone screenshot is already a permanent regression test:
a validated Android network cannot be declared bad solely because one diagnostic endpoint fails.

## Continue without another user install

1. Keep integrating high-value safe features.
2. Improve local evidence/receipts so field failures are easier to diagnose.
3. Keep Autopilot profiles persistent and bounded.
4. Preserve Mobile Vault, anti-flapping and fail-open rules.
5. Do not overwrite the stable installer for a micro-change.

## Promotion rule

Promote the next APK only when the batch is materially more complete than RC1 and CI/regression gates pass.
