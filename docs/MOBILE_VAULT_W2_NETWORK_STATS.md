# KINLINK W2 — optional Android NetworkStats evidence

This block adds an optional, capability-gated Android `NetworkStatsManager`
path without making Usage Access a KINLINK dependency.

## Safety contract

1. NetworkStats queries are rejected on the main/UI thread.
2. A plan-cycle start is required before any query is attempted.
3. Missing Usage Access is a normal fail-open state: the rest of KINLINK keeps
   working and Mobile Vault does not invent consumption.
4. This adapter deliberately queries only aggregate mobile usage and therefore
   always labels its evidence `DEVICE_MOBILE_AGGREGATE`.
5. Aggregate NetworkStats evidence is never promoted to `PLAN_EXACT`; it
   therefore cannot spend from a guessed single-plan balance.
6. Per-plan attribution stays HOLD/UNKNOWN until a separately authorized source
   can prove the plan identity and required platform access.
7. Stored NetworkStats evidence contains bytes, cycle start, observation
   timestamp, confidence and adapter version; it stores no carrier/SIM identity.
8. Evidence from a different plan cycle is ignored by Mobile Vault.
9. Platform denial or malformed counters yield HOLD/UNKNOWN semantics rather
   than zero, guessed usage, or a connectivity failure.

This adapter complements, but does not replace, user-reconciled evidence or
carrier adapters.
