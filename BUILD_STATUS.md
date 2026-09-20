# KINLINK build status

## Proven checkpoint

P0 handoff/resource baseline `56169dbd3fc691cc7233d46c9c44edda6e3e633e` is fully green.
Watchdog/stale-network hardening is under CI from `dbb107cd36598f5360bc893b45d64e43abdaf6f7`.

## Current lifecycle batch

Added:
- durable service START/STOP receipts;
- 10-minute restart-storm window;
- active recovery allowed for at most 3 service creations in that window;
- on the 4th+ creation, KINLINK degrades to observation-only;
- no routing/mobile intervention is introduced.

This converts repeated lifecycle instability into a bounded safe mode rather than repeated active recovery.
