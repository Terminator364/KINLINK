# NEXT_UNCOMMITTED_ACTION

## P0 field incident

Observed: after leaving Wi-Fi coverage, mobile data appeared unusable.
The installed application must never be able to influence normal Android mobile-data handoff.

## Immediate machine action

1. Remove every use of `reportNetworkConnectivity`.
2. Keep automatic actions strictly Wi-Fi-only.
3. Preserve a runtime transport re-check immediately before every automatic action.
4. Add regression tests proving cellular => NO_ACTION and probe results => no framework connectivity report.
5. Run full CI and inspect the repository for forbidden network-binding/routing APIs.

## Delivery rule

Do not ask the user to install RC2 again.
The next installer must be a consolidated, stable-signed candidate that passes this P0 handoff gate.
