# KINLINK build status

## Stable rollback baseline

**0.4.0-rc1** remains stable-signed, Drive-promoted and recoverable.

It already contains the permanent regression fix for the field contradiction:
Android VALIDATED cannot be downgraded merely because one external probe fails.

## Development head

Commit: `7feaec6975638fe224614757e16977ff91302917`  
CI run: `35532459908` — **PASS**

New integrated scope:
- persistent Autopilot profiles;
- Conservative / Balanced / Maximum Stability selector;
- selected profile survives app restart;
- adaptive policy uses the persisted profile;
- technical diagnostics expose the active profile.

## Delivery discipline

Do **not** make the user install this isolated increment.
Continue accumulating coherent product work, test it in CI, and promote one consolidated stable-signed installer later.
