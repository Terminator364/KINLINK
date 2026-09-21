# KINLINK — B EXPANSION V4 (B81-B90)

Status: canonical research-depth layer appended to B31-B80. The global macro denominator remains 80.

## Why this layer matters

KINLINK now targets Android 17 / API 37. Android 17 changes the permission model for local-network communication: direct LAN TCP/UDP/discovery access can require the runtime `ACCESS_LOCAL_NETWORK` permission. This does **not** mean KINLINK should ask for it at first launch. The correct product behavior is capability discovery plus just-in-time permission only for a feature that genuinely needs direct LAN access.

The future Strong Stabilizer also needs more explicit Android VPN contracts: LAN preservation, MTU safety, always-on qualification, foreground-service legality and capability discovery.

## B81-B90

### B81 — Android 17 local-network permission gate
**Parent macros:** K041, K061, K064  
**Stage:** DESIGN_ONLY

Because KINLINK targets API 37, any future direct LAN socket/discovery feature must be capability-gated behind Android 17 ACCESS_LOCAL_NETWORK or an appropriate system-mediated alternative. Lite external-Internet observation must not request this permission unnecessarily.

**Acceptance:** No direct LAN feature starts on API 37+ unless permission/capability state is explicit. Existing Internet-only Wi-Fi probes remain classified separately and do not trigger a LAN permission prompt.

**Primary source:** https://developer.android.com/about/versions/17/behavior-changes-17

### B82 — Local-vs-Internet operation classifier
**Parent macros:** K006, K041, K061  
**Stage:** DESIGN_ONLY

Classify every network operation as LOCAL_LAN, PUBLIC_INTERNET, PLATFORM_OBSERVATION or VPN_DATA_PLANE before activation so permission, cost and privacy rules are applied correctly.

**Acceptance:** Static/runtime policy tests prove public connectivity checks are never mistaken for LAN access and LAN operations cannot bypass their permission gate.

**Primary source:** https://developer.android.com/about/versions/16/behavior-changes-16

### B83 — Just-in-time LAN permission UX
**Parent macros:** K002, K041, K058  
**Stage:** DESIGN_ONLY

If a future router/LAN feature needs ACCESS_LOCAL_NETWORK, request it only when the user invokes that feature, explain the exact benefit, and keep all unrelated KINLINK functions usable when denied.

**Acceptance:** Fresh install has no gratuitous LAN permission prompt; denial leaves observer/mobile/autopilot-safe functions operational.

**Primary source:** https://developer.android.com/about/versions/17/behavior-changes-17

### B84 — VPN LAN-preservation route policy
**Parent macros:** K007, K019, K023  
**Stage:** DESIGN_ONLY

For API 33+, future Strong Stabilizer should model explicit LAN route exclusions with VpnService.Builder.excludeRoute where preservation is required, instead of accidentally tunnelling/breaking local traffic.

**Acceptance:** IPv4/IPv6 LAN fixtures prove selected local prefixes bypass the VPN while Internet routes remain controlled; older APIs fail closed or use a separately proven route plan.

**Primary source:** https://developer.android.com/reference/android/net/VpnService.Builder#excludeRoute(android.net.IpPrefix)

### B85 — VPN MTU and packet-size safety
**Parent macros:** K019, K023, K025  
**Stage:** DESIGN_ONLY

Strong Stabilizer must treat MTU as an explicit compatibility parameter, detect black-hole/fragmentation symptoms and roll back rather than hard-code an aggressive value.

**Acceptance:** Test matrix covers IPv4/IPv6, small/large MTU, truncated packets and PMTU-related failure without loops or silent data loss.

**Primary source:** https://developer.android.com/reference/android/net/VpnService.Builder#setMtu(int)

### B86 — Always-on support declaration gate
**Parent macros:** K025, K056  
**Stage:** DESIGN_ONLY

Do not advertise always-on VPN support merely because Android defaults the metadata to true. Strong Stabilizer must explicitly opt in only after reboot, upgrade, revoke, lockdown and escape-path qualification.

**Acceptance:** Manifest metadata and release gate agree; an unqualified VPN build cannot be exposed as always-on capable.

**Primary source:** https://developer.android.com/reference/android/net/VpnService#SERVICE_META_DATA_SUPPORTS_ALWAYS_ON

### B87 — Foreground-service type and boot legality audit
**Parent macros:** K045, K058  
**Stage:** PARTIAL

Every long-lived observer/VPN service must have an Android-version-aware foreground-service type/start-source contract, including BOOT_COMPLETED restrictions and user-visible notification semantics.

**Acceptance:** CI/field tests cover cold start, boot, package replacement, user stop, background start denial and graceful degraded behavior across supported API levels.

**Primary source:** https://developer.android.com/develop/background-work/services/fgs/service-types

### B88 — Data Saver state as policy evidence
**Parent macros:** K014, K060  
**Stage:** IMPLEMENTED_UNQUALIFIED

Observe ConnectivityManager restrictBackgroundStatus and expose DISABLED/WHITELISTED/ENABLED/UNKNOWN as a capability fact. Future background work must respect it rather than infer cost only from transport.

**Acceptance:** PlatformCapabilityDiscovery reports Data Saver without changing settings; policy tests map Android constants deterministically.

**Primary source:** https://developer.android.com/reference/android/net/ConnectivityManager#getRestrictBackgroundStatus()

### B89 — Optional NetworkStats usage-access lane
**Parent macros:** K017, K041  
**Stage:** PARTIAL

Discover Usage Access explicitly and keep richer NetworkStatsManager accounting optional/off-main-thread. Absence of Usage Access must never block core KINLINK.

**Acceptance:** Capability snapshot distinguishes granted/not-granted; no NetworkStats query runs on the UI thread and no operator-balance claim is derived from these counters.

**Primary source:** https://developer.android.com/reference/android/app/usage/NetworkStatsManager

### B90 — Runtime platform capability inventory
**Parent macros:** K041, K053, K058  
**Stage:** IMPLEMENTED_UNQUALIFIED

Before optional features activate, maintain a read-only capability snapshot: API level, Wi-Fi/telephony presence, Android 17 LAN-permission state, Data Saver, Usage Access, VpnService route-exclusion support and advanced-diagnostics eligibility.

**Acceptance:** Discovery performs no permission request, VPN start or socket; unit tests cover policy boundaries and technical diagnostics expose capability state.

**Primary source:** https://developer.android.com/guide/topics/permissions/overview


## Concrete implementation started in 0.8

The active 0.8 branch now contains a read-only `PlatformCapabilityDiscovery`:
- API level;
- Wi-Fi and telephony hardware presence;
- Android 17 local-network permission state;
- Data Saver state;
- optional Usage Access state;
- VPN route-exclusion support;
- explicit Lite-Observer ineligibility for advanced ConnectivityDiagnostics.

It performs **no permission request, VPN start or socket operation**.

## Scoring rule

B81-B90 deepen existing macros. They do not change the 80-macro completion denominator unless an explicit deduplication review proves an independent macro capability.
