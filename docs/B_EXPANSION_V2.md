# KINLINK — B EXPANSION V2 (B31-B60)

Status: canonical depth layer under the full A+B+C specification.

B is not a wishlist. Every B item must state a platform reality, concrete capability, acceptance test, cost/risk and rollback/abstention rule.

## Critical research correction before adding more capability

Android's `ConnectivityDiagnosticsManager` does **not** provide useful callbacks to an arbitrary observer app merely because registration succeeds. Android documents that callbacks are invoked only for apps that offer connectivity to the user, including active VPNs, carrier apps with active subscriptions and Wi-Fi suggesters; location permission may also be necessary for location-bound networks.

Therefore:
- KINLINK Lite Observer MUST NOT claim ConnectivityDiagnostics data-stall evidence is active;
- the adapter may exist but remains dormant until KINLINK qualifies (for example, an active KINLINK VPN data plane);
- Lite mode continues with `NetworkCallback`, `NetworkCapabilities` and `LinkProperties`;
- absence of ConnectivityDiagnostics callbacks is not evidence that there were no stalls.

This is a research-driven correction to the earlier B1 assumption.

## B31-B60

### B31 — Diagnostics eligibility ladder
**Parent macros:** K003, K009  
**Current stage:** PARTIAL

Separate universally available NetworkCallback/NetworkCapabilities/LinkProperties from ConnectivityDiagnostics callbacks that Android only delivers to eligible connectivity providers. Never call a silent registration success 'active diagnostics'.

**Acceptance:** Lite Observer proves Tier 0; Tier 1 only when KINLINK itself qualifies as active VPN/carrier/Wi-Fi suggester and required permissions are explicit.

**Source/provenance:** https://developer.android.com/reference/android/net/ConnectivityDiagnosticsManager

### B32 — Probe budget and ambiguity resolver
**Parent macros:** K003, K006, K060  
**Current stage:** DESIGN_ONLY

Keep zero-probe observation as default. Allow only explicitly budgeted tiny active probes when passive evidence cannot distinguish WAN/DNS/remote-service failure; mobile probes default OFF.

**Acceptance:** Every probe class has byte cap, transport policy, cooldown, user-visible rationale, privacy review and negative control.

**Source/provenance:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B33 — Monotonic causal windows
**Parent macros:** K029, K031, K032  
**Current stage:** PARTIAL

Use wall time for human reports and monotonic elapsed time for durations, cooldowns, evidence windows and relapse logic.

**Acceptance:** Clock changes cannot alter causal duration, cooldown or at-most-once reporting.

**Source/provenance:** https://developer.android.com/reference/android/os/SystemClock

### B34 — Privacy-preserving network identity
**Parent macros:** K004, K005, K043, K061  
**Current stage:** DESIGN_ONLY

Represent trusted/home networks using pseudonymous local identifiers, confidence and recency; raw SSID/BSSID should not become general telemetry.

**Acceptance:** Identity survives normal reconnects without exposing raw identifiers in exports; reset/forget is explicit.

**Source/provenance:** https://developer.android.com/develop/connectivity/wifi/wifi-permissions

### B35 — Confidence calibration and abstention
**Parent macros:** K003, K006, K013  
**Current stage:** DESIGN_ONLY

Every diagnosis exposes evidence strength and can return UNKNOWN/HOLD rather than force a confident label.

**Acceptance:** Calibration tests include false-positive/false-negative cases; UI never converts internal confidence into unsupported certainty.

**Source/provenance:** https://developer.android.com/reference/android/net/NetworkCapabilities

### B36 — Incident fingerprints and recurrence clustering
**Parent macros:** K031, K077  
**Current stage:** DESIGN_ONLY

Create privacy-safe fingerprints for recurring combinations of transport, DNS, route, stall, handoff and resource symptoms.

**Acceptance:** Clusters are reproducible from durable events and never rely on payload inspection.

**Source/provenance:** https://aclanthology.org/2026.findings-acl.2069/

### B37 — Deterministic Personal Network Twin replay
**Parent macros:** K003, K028, K029  
**Current stage:** DESIGN_ONLY

Replay recorded event sequences through candidate policies offline before activation; compare candidate decisions with historical champion behavior.

**Acceptance:** Same event trace + same policy version yields same decisions; replays cannot mutate production state.

**Source/provenance:** https://martinfowler.com/eaaDev/EventSourcing.html

### B38 — Feature flags, kill switches and staged policy rollout
**Parent macros:** K025, K048, K054  
**Current stage:** DESIGN_ONLY

Every risky policy/action family gets a durable enablement flag, rollback target and blast-radius boundary.

**Acceptance:** One switch can disable a harmful policy without reinstall; state is versioned and read back.

**Source/provenance:** https://developer.android.com/topic/architecture

### B39 — OEM survival qualification matrix
**Parent macros:** K045, K053, K058  
**Current stage:** DESIGN_ONLY

Qualify long-running observer behavior across Samsung/One UI and representative Android API levels, including reboot, idle, battery saver and process death.

**Acceptance:** No OEM assumption is promoted from emulator-only evidence.

**Source/provenance:** https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start

### B40 — Existing VPN / Always-on / Lockdown coexistence
**Parent macros:** K019, K056  
**Current stage:** DESIGN_ONLY

Detect incompatible VPN state and avoid breaking user networking. Strong Stabilizer must coexist or fail closed when another VPN/lockdown policy owns the path.

**Acceptance:** Never silently replace an existing VPN; user escape path is always available.

**Source/provenance:** https://developer.android.com/reference/android/app/admin/DevicePolicyManager

### B41 — Per-app canary rollout for Strong Stabilizer
**Parent macros:** K019, K022, K023  
**Current stage:** DESIGN_ONLY

Start with an allowlist of selected apps instead of whole-device interception to constrain blast radius.

**Acceptance:** Allowed/disallowed sets are mutually exclusive, validated against installed packages and rollback to native networking.

**Source/provenance:** https://developer.android.com/reference/android/net/VpnService.Builder

### B42 — TUN parser and data-plane fuzzing
**Parent macros:** K019, K020, K079  
**Current stage:** DESIGN_ONLY

Before any production VPN data plane, fuzz IPv4/IPv6/UDP/TCP/fragment/truncation/error paths and enforce memory bounds.

**Acceptance:** Malformed packets cannot crash, loop, leak payloads or wedge teardown.

**Source/provenance:** https://source.android.com/docs/security/test

### B43 — Transport migration awareness
**Parent macros:** K019, K023, K066  
**Current stage:** DESIGN_ONLY

Design future data plane so it does not break QUIC migration and can observe handoffs without inventing seamlessness it cannot guarantee.

**Acceptance:** Tests cover address changes, NAT rebinding, path validation and rollback behavior.

**Source/provenance:** https://www.rfc-editor.org/rfc/rfc9000.html

### B44 — Happy-Eyeballs strategy for KINLINK-owned connections
**Parent macros:** K063, K064  
**Current stage:** DESIGN_ONLY

Where KINLINK itself opens diagnostic/control connections, prefer bounded IPv6/IPv4 racing rather than serial long stalls.

**Acceptance:** Only KINLINK-owned sockets are affected; no claim of changing other apps without VPN/data plane.

**Source/provenance:** https://www.rfc-editor.org/rfc/rfc8305.html

### B45 — Multi-path research gate, not a fake feature
**Parent macros:** K019, K023, K066  
**Current stage:** DESIGN_ONLY

Treat MPTCP/multi-path as a research/feasibility gate. Android app-space must not claim multipath aggregation unless the chosen stack and peers actually support it.

**Acceptance:** Prototype must prove compatibility, cost, battery and server requirements before inclusion.

**Source/provenance:** https://www.rfc-editor.org/rfc/rfc8684.html

### B46 — Meteredness inheritance contract
**Parent macros:** K019, K060  
**Current stage:** DESIGN_ONLY

Future VPN must preserve or deliberately mark meteredness so downstream apps do not assume free data when underlying mobile is paid.

**Acceptance:** Automated tests compare underlying and VPN metered state across Wi-Fi/mobile transitions.

**Source/provenance:** https://developer.android.com/reference/android/net/VpnService.Builder#setMetered(boolean)

### B47 — Multi-SIM / subscription-aware policy
**Parent macros:** K014, K015, K016, K060  
**Current stage:** DESIGN_ONLY

Model multiple mobile subscriptions separately where platform permission/role permits; never merge quotas or confidence across SIMs.

**Acceptance:** Unknown subscription identity stays UNKNOWN; no subscriber identifiers are exported.

**Source/provenance:** https://developer.android.com/reference/android/telephony/SubscriptionManager

### B48 — Resource SLO envelopes and percentile budgets
**Parent macros:** K044, K058, K075  
**Current stage:** PARTIAL

Move from single snapshots to rolling P50/P95 budgets for PSS, CPU, wakeups, battery drain and thermal throttling.

**Acceptance:** Promotion fails if benefit is smaller than resource cost or if tail usage violates weak-device budgets.

**Source/provenance:** https://developer.android.com/topic/performance/power

### B49 — Telemetry compaction and rebuildable indexes
**Parent macros:** K032  
**Current stage:** DESIGN_ONLY

Keep append-only canonical facts but compact/reindex derived views; indexes may be deleted and rebuilt from canonical events.

**Acceptance:** Compaction never removes evidence required for active gates or unresolved incidents.

**Source/provenance:** https://www.sqlite.org/wal.html

### B50 — Diagnostic redaction and privacy classes
**Parent macros:** K033, K043  
**Current stage:** PARTIAL

Assign privacy classes to every exported field and enforce allowlist-based diagnostic export.

**Acceptance:** Secrets, raw payloads and unnecessary identifiers cannot enter export even after new modules are added.

**Source/provenance:** https://developer.android.com/privacy-and-security

### B51 — Reproducible release metadata and SBOM
**Parent macros:** K047, K048  
**Current stage:** DESIGN_ONLY

Record exact dependencies/toolchain/build provenance and generate a dependency inventory for every promoted artifact.

**Acceptance:** A field artifact can be traced back to source commit, toolchain, signer and dependency set.

**Source/provenance:** https://slsa.dev/spec/v1.0/

### B52 — Backward-compatible telemetry/schema migration
**Parent macros:** K032, K046  
**Current stage:** PARTIAL

Version every durable schema; migrations are idempotent, tested across skips and reversible where feasible.

**Acceptance:** Old field data remains readable or has an explicit migration/tombstone path.

**Source/provenance:** https://developer.android.com/training/data-storage/room/migrating-db-versions

### B53 — Capability discovery before policy activation
**Parent macros:** K041, K056  
**Current stage:** DESIGN_ONLY

At runtime discover API level, permissions, VPN ownership, Usage Access, battery/thermal support and optional diagnostics eligibility before enabling features.

**Acceptance:** UI and policy expose unavailable/optional/ready states instead of failing later.

**Source/provenance:** https://developer.android.com/guide/topics/permissions/overview

### B54 — Explainable action receipts
**Parent macros:** K029, K032, K035  
**Current stage:** PARTIAL

Every intervention should answer WHY, WHAT, EXPECTED BENEFIT, COST, RESULT and WHY NEXT/STOP in human and machine-readable forms.

**Acceptance:** No action without durable receipt; UI can show concise human explanation without raw enum leakage.

**Source/provenance:** https://cloudevents.io/

### B55 — Emergency disable and native-network escape
**Parent macros:** K040, K019, K025  
**Current stage:** DESIGN_ONLY

Provide a single local action to disable risky automation/data plane and return immediately to Android-native networking.

**Acceptance:** Must work without Internet and without cloud/BCP availability.

**Source/provenance:** https://developer.android.com/develop/connectivity/vpn

### B56 — Automatic harm rollback
**Parent macros:** K029, K078  
**Current stage:** PARTIAL

If post-action evidence crosses harm thresholds, stop the policy, restore prior safe state and persist a harmful-outcome receipt.

**Acceptance:** Rollback is bounded, idempotent and cannot oscillate indefinitely.

**Source/provenance:** https://martinfowler.com/bliki/CircuitBreaker.html

### B57 — Network impairment replay lab
**Parent macros:** K027, K052, K053  
**Current stage:** DESIGN_ONLY

Turn recurring field failures into deterministic CI scenarios: flapping, latency, loss, DNS blackhole, WAN-down/LAN-up, captive, IPv6 partial, process kill and resource pressure.

**Acceptance:** Each field incident can acquire a replay fixture and regression obligation.

**Source/provenance:** https://developer.android.com/studio/run/emulator-networking

### B58 — Human-interaction budget
**Parent macros:** K001, K002, K041, K054  
**Current stage:** DESIGN_ONLY

Treat taps, screenshots, reinstalls, permission trips and copy/paste as a measurable scarce resource, especially under fatigue/poor connectivity.

**Acceptance:** A release plan states expected human actions and rejects avoidable repeated steps.

**Source/provenance:** internal:C-feedback

### B59 — Bounded BCP diagnostic bridge
**Parent macros:** K051, K052  
**Current stage:** DESIGN_ONLY

Expose signed, allowlisted, read-only diagnostic/receipt retrieval to the wider personal control plane before any remote actuation.

**Acceptance:** No arbitrary shell, no secret export, local app remains correct if bridge is offline.

**Source/provenance:** internal:BCP/APIAX07

### B60 — Dead-end / impossibility registry
**Parent macros:** K055  
**Current stage:** DESIGN_ONLY

Persist disproven approaches, platform restrictions and failed assumptions so future conversations do not rediscover the same dead end.

**Acceptance:** Each entry records premise, evidence, scope, date, supersession condition and safe alternative.

**Source/provenance:** internal:memory-doctrine


## Promotion rule

B31-B60 enrich existing macro capabilities. They do **not** automatically inflate the product denominator. A new macro is created only when a deduplication review demonstrates independent user/product value with its own acceptance boundary.

## Research references

- Android ConnectivityDiagnosticsManager: https://developer.android.com/reference/android/net/ConnectivityDiagnosticsManager
- Android NetworkStatsManager: https://developer.android.com/reference/android/app/usage/NetworkStatsManager
- Android VpnService / Builder: https://developer.android.com/reference/android/net/VpnService and https://developer.android.com/reference/android/net/VpnService.Builder
- Android foreground-service restrictions/types: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start and https://developer.android.com/develop/background-work/services/fgs/service-types
- RFC 8305 Happy Eyeballs v2: https://www.rfc-editor.org/rfc/rfc8305.html
- RFC 9000 QUIC migration: https://www.rfc-editor.org/rfc/rfc9000.html
- RFC 8684 Multipath TCP: https://www.rfc-editor.org/rfc/rfc8684.html
- ACL 2026 agent-memory survey: https://aclanthology.org/2026.findings-acl.2069/
- CloudEvents event identity/provenance: https://github.com/cloudevents/spec
