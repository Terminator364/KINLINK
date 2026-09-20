# KINLINK Canonical Specification v0.1

## Mission
KINLINK is a low-overhead Android network-autopilot that observes current connectivity, infers the failure domain, applies only bounded reversible optimizations, protects paid mobile data, preserves LAN operation, and records enough evidence to improve subsequent releases.

## User experience target
One provisioning flow, then autopilot. Daily use should normally require no manual tuning.

## Connectivity truth model
KINLINK must maintain independent state for:
1. transport availability (Wi-Fi, cellular, LAN, none),
2. local link quality,
3. Internet validation,
4. failure domain,
5. mobile budget state,
6. user/application intent class,
7. battery/thermal budget,
8. confidence in diagnosis.

## Hard rules
- No hidden mobile-data spending beyond configured envelope.
- No automatic mobile speed tests.
- No permanent dependency on cloud/VPS.
- No root requirement.
- No VPN lockdown by default.
- No action without rollback path unless action is observational only.
- If KINLINK data plane is unhealthy, release control back to Android.
- If balance is exhausted or expired, stop active mobile recovery probes.
- If WAN fails but LAN remains healthy, keep LAN routes usable.

## Primary contexts
- HOME_WIFI
- TRUSTED_WIFI
- UNKNOWN_WIFI
- MOBILE_RESILIENT
- CAPTIVE_PORTAL
- PARTIAL_CONNECTIVITY
- BROWNOUT
- OFFLINE
- RECOVERY

## User-visible profiles
- Conservative
- Balanced (default)
- Maximum Stability

The internal policy engine may use many parameters; the user should not have to configure them individually.
