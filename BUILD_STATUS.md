# KINLINK build status

## Proven machine checkpoint

Commit `56169dbd3fc691cc7233d46c9c44edda6e3e633e` passed Android CI and design-lint.

Validated together:
- hard fail-open mobile handoff;
- static CI ban on routing/network ownership APIs;
- passive handoff receipts;
- battery, severe thermal and Android low-memory guards.

## Current integration batch

Added runtime watchdog enforcement:
- 5 second hard recovery deadline;
- active-network identity re-check before post-probe work;
- immediate abort if Android changes away from the Wi-Fi being examined;
- no stale refresh after Wi-Fi→mobile handoff;
- receipt for watchdog timeout or transport-change abort.

## Delivery

No installer promotion until this batch passes CI and the remaining lifecycle/resource gates are consolidated.
