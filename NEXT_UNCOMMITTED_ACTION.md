# NEXT_UNCOMMITTED_ACTION

## Stable field baseline
0.6.0-rc3 remains installed, signed, Drive-canonical and field-validated.

## Active DEV line
0.7.0-dev / versionCode 8.

## Newly integrated
1. passive root-cause classification;
2. passive DNS metadata observation;
3. interruption duration/severity evidence;
4. cause-aware guidance and notification;
5. passive cause-transition history;
6. qualitative session health;
7. once-per-version post-update self-test:
   - DB schema >= 3;
   - RecoveryMode readable;
   - first NetworkCallback observed.

## Current machine gate
Finish complete CI for the self-test head.

## Next material block
- resource-budget qualification hooks/evidence;
- background churn/wakeup discipline;
- richer longitudinal outage/cause summary;
- only then assess readiness for a consolidated next release candidate.

Do not promote 0.7.0-dev and do not ask the user to install it yet.
