# Network State Machine

## Top-level states
- QUIET
- WATCH
- DEGRADED
- BROWNOUT
- OFFLINE
- RECOVERING

## Mobile budget states
- BUNDLE_OK
- BUNDLE_LOW
- BUNDLE_EXHAUSTED
- BUNDLE_EXPIRED
- BALANCE_UNKNOWN

## Wi-Fi trust states
- HOME_WIFI
- TRUSTED_WIFI
- UNKNOWN_WIFI
- CAPTIVE_PORTAL

## Example transition: mobile -> home Wi-Fi
1. Wi-Fi network appears.
2. Validate local gateway.
3. Validate Internet state.
4. Run bounded micro-probe only if existing evidence is insufficient.
5. Enter probation.
6. Route new eligible flows to Wi-Fi.
7. Drain old mobile flows where feasible.
8. Lock Mobile Vault for automatic rescue unless policy explicitly permits assist.

## Example: mobile data exhausted
Evidence may include carrier balance, plan-expiry knowledge, repeated no-data state, and no captive portal.
When confidence reaches threshold:
- set BUNDLE_EXHAUSTED or BUNDLE_EXPIRED;
- disable active mobile recovery probes;
- do not race DNS or open test flows;
- keep SIM/telephony state observable;
- wait for Wi-Fi or balance refresh.

## Anti-flapping
Transitions must use asymmetric hysteresis:
- prefer switching to verified unmetered Wi-Fi quickly;
- require stronger evidence before abandoning Wi-Fi for paid mobile data.
