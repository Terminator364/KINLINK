# KINLINK build status

## Proven hardened baseline

All P0 handoff, watchdog, resource, lifecycle and post-handoff settling batches through `7a35ef299257c30f7548cc0fe3d9a447e69662af` are green.

## Current integration

In addition to handoff outcome observability, KINLINK now gains a persistent user fail-open control:
- AUTOMATIC mode;
- OBSERVATION_ONLY safe mode;
- safe mode blocks automatic recovery and manual Wi-Fi optimization;
- safe mode does not stop passive diagnostics or telemetry;
- current mode is visible in technical details and foreground notification.

No uninstall is required to disable active KINLINK behavior in future field testing.
