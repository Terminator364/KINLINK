# KINLINK — B EXPANSION V3 (B61-B80)

Status: canonical research-depth layer appended to B31-B60. These are concrete platform/reliability requirements, not marketing ideas.

## Fresh platform findings integrated

Android's current connectivity guidance changes several engineering details that matter directly to KINLINK:

1. **Do not synchronously query capabilities/link properties from `onAvailable`.** Android documents a race; wait for `onCapabilitiesChanged` / `onLinkPropertiesChanged`.
2. **A `Network` object is generation-specific.** Reconnecting to the same physical network yields a new object; stale callbacks/evidence must not cross generations.
3. **Transport name is not quality or cost.** Wi-Fi is not automatically unmetered/better, and VPN networks can expose multiple transports.
4. **Bandwidth values are estimates, not throughput proof.** Android 12+ may derive them from historical weighted averages by carrier/SSID/network type/signal.
5. **Telephony signal can be cached/stale** under power saving and can be unavailable.
6. **VPN permission is revocable.** `onRevoke()` must tear down cleanly; `protect()` is required to prevent tunnel self-routing loops when routes overlap.
7. **Underlying-network declarations must follow reality** if a future VPN explicitly binds upstream sockets to selected Networks.

## B61-B80

### B61 — Callback-first race discipline
**Parent macros:** K003, K008  
**Stage:** PARTIAL

Never synchronously query NetworkCapabilities/LinkProperties from onAvailable. Assemble truth from onCapabilitiesChanged/onLinkPropertiesChanged for the same still-active Network generation.

**Acceptance:** No synchronous capability/link-property query remains in NetworkObserver callback flow; publish requires both callback snapshots and active-network identity before/after reduction.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B62 — Callback registration budget
**Parent macros:** K008, K058  
**Stage:** DESIGN_ONLY

Track every registered NetworkCallback as a scarce resource, unregister deterministically, and reject accidental duplicate registration.

**Acceptance:** Lifecycle test proves one observer registration per owner, deterministic unregister on stop, and no callback-count leak after repeated start/stop.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B63 — Default-network generation identity
**Parent macros:** K003, K066  
**Stage:** PARTIAL

Treat each Android Network object as a generation token. Reconnecting to the same AP/carrier is a new generation; stale callbacks cannot mutate the current truth.

**Acceptance:** Loss/reconnect tests prove old Network callbacks cannot cancel or overwrite state of the new default Network.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B64 — Multi-transport truth model
**Parent macros:** K003, K063  
**Stage:** DESIGN_ONLY

Do not force one physical-transport assumption when NetworkCapabilities can represent VPN plus Wi-Fi/mobile or other combinations. Preserve logical and underlying transport evidence separately.

**Acceptance:** Truth schema can express logical VPN + underlying transport set without collapsing it to one misleading label.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B65 — Bandwidth-estimate prior, never throughput verdict
**Parent macros:** K013, K031  
**Stage:** PARTIAL

Treat getLinkDownstream/UpstreamBandwidthKbps as Android estimates/prior information, not current measured throughput or user QoE.

**Acceptance:** No user-facing quality verdict can be produced from bandwidth estimate alone; field history/user report can override it.

**Primary source:** https://developer.android.com/about/versions/12/features

### B66 — Meteredness independent of transport
**Parent macros:** K014, K060  
**Stage:** PARTIAL

Never infer cost from Wi-Fi vs cellular. Use NET_CAPABILITY_NOT_METERED and explicit user/carrier budget state.

**Acceptance:** Metered Wi-Fi and unmetered cellular test fixtures do not follow transport-name assumptions.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B67 — Connectivity-thread work budget
**Parent macros:** K008, K058  
**Stage:** DESIGN_ONLY

Keep ConnectivityManager callback work minimal; longer ledger/policy/report work must be serialized outside the connectivity callback thread without unbounded executors.

**Acceptance:** Callback handler latency budget is measured; long operations are offloaded to one bounded worker and cleanly shut down.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B68 — Radio signal freshness semantics
**Parent macros:** K006, K031  
**Stage:** DESIGN_ONLY

TelephonyManager signal strength can be cached/stale under power saving. Record freshness/source and never equate one cached signal sample with current radio quality.

**Acceptance:** UI/diagnostics expose stale/unknown rather than pretending a cached modem sample is fresh.

**Primary source:** https://developer.android.com/reference/android/telephony/TelephonyManager

### B69 — Permission-aware telephony evidence ladder
**Parent macros:** K006, K041  
**Stage:** DESIGN_ONLY

Use only telephony observations available under ordinary user-granted permissions. Carrier-privileged/MODIFY_PHONE_STATE APIs remain out of normal KINLINK requirements.

**Acceptance:** Capability discovery marks each telephony signal as AVAILABLE/OPTIONAL/PRIVILEGED/UNAVAILABLE and degrades gracefully.

**Primary source:** https://developer.android.com/reference/android/telephony/TelephonyManager

### B70 — Multi-SIM observer isolation
**Parent macros:** K016, K047  
**Stage:** DESIGN_ONLY

When permissions allow, create per-subscription TelephonyManager/callback state. Never merge evidence, budgets or carrier confidence across SIMs.

**Acceptance:** Two-SIM simulation keeps subscription state and evidence windows separate and survives default-data-SIM changes.

**Primary source:** https://developer.android.com/reference/android/telephony/TelephonyManager

### B71 — Always-on and lockdown VPN awareness
**Parent macros:** K019, K056  
**Stage:** DESIGN_ONLY

Strong Stabilizer must explicitly detect/handle always-on and lockdown semantics; a bad VPN must not accidentally strand networking.

**Acceptance:** Tests cover normal VPN, always-on, lockdown, reboot and service restart; escape/recovery behavior is documented and bounded.

**Primary source:** https://developer.android.com/reference/android/net/VpnService

### B72 — VPN consent revocation path
**Parent macros:** K019, K025, K056  
**Stage:** DESIGN_ONLY

VpnService permission can be revoked at any time. onRevoke must close descriptors/tunnel state, persist receipt and return to Android-native networking cleanly.

**Acceptance:** Revocation fault-injection proves no stuck TUN, loop, orphan worker or false active status.

**Primary source:** https://developer.android.com/reference/android/net/VpnService

### B73 — Underlying-network declaration integrity
**Parent macros:** K019, K023, K066  
**Stage:** DESIGN_ONLY

If future VPN upstream sockets are explicitly bound to Networks, call setUnderlyingNetworks whenever the set changes and preserve preference ordering.

**Acceptance:** Handoff tests prove system-visible underlying networks match actual protected upstream sockets before/after Wi-Fi↔mobile change.

**Primary source:** https://developer.android.com/reference/android/net/VpnService

### B74 — VPN loop-prevention contract
**Parent macros:** K019, K025  
**Stage:** DESIGN_ONLY

Every tunnel/upstream socket whose destination falls inside VPN routes must be protect()'ed or equivalently excluded to prevent self-routing loops.

**Acceptance:** Fault test intentionally covers tunnel destination with VPN route and proves no infinite loop/resource runaway.

**Primary source:** https://developer.android.com/reference/android/net/VpnService

### B75 — Visible network is not necessarily usable
**Parent macros:** K003, K006  
**Stage:** PARTIAL

A network seen by callback may lack Internet or be restricted. Internet usability requires relevant capabilities such as INTERNET/VALIDATED and policy context.

**Acceptance:** Network-present-but-restricted fixtures do not become 'Internet available'.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B76 — No background-network activation in Lite mode
**Parent macros:** K010, K060  
**Stage:** MACHINE_PROVEN

Lite KINLINK observes the default network and must not request/bring up alternate paid networks. CHANGE_NETWORK_STATE stays forbidden until an explicitly redesigned subsystem justifies it.

**Acceptance:** Manifest/API fences reject requestNetwork/CHANGE_NETWORK_STATE in Lite build.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B77 — Telephony feature detection before access
**Parent macros:** K041, K058  
**Stage:** DESIGN_ONLY

Check FEATURE_TELEPHONY_* support and nullable/unsupported behavior before telephony access, especially on tablets or unusual devices.

**Acceptance:** No-telephony device fixture remains fully functional in observer mode with explicit unavailable capability state.

**Primary source:** https://developer.android.com/reference/android/telephony/TelephonyManager

### B78 — Transport-history priors by context, not hard constants
**Parent macros:** K004, K028, K031  
**Stage:** DESIGN_ONLY

Use local historical priors per trusted context/signal band/time window to interpret Android estimates, instead of fixed assumptions such as Wi-Fi > mobile.

**Acceptance:** Priors remain local, decay with age, are resettable, and never override direct recent field evidence.

**Primary source:** https://developer.android.com/about/versions/12/features

### B79 — Reconnect-generation reset semantics
**Parent macros:** K003, K032, K066  
**Stage:** DESIGN_ONLY

Because reconnect yields a new Network object, evidence windows tied to the old generation must close/supersede rather than silently continue.

**Acceptance:** Reconnect simulation proves latency/stall/handoff windows are attributed to the correct generation and stale evidence cannot promote a new link.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state

### B80 — Proof hierarchy for network quality
**Parent macros:** K013, K031, K052  
**Stage:** PARTIAL

Formalize source precedence: explicit user incident + sustained local experience history > direct current validated state > platform capability estimates > static defaults. Confidence is source-aware and can abstain.

**Acceptance:** Contradiction tests show optimistic bandwidth estimates cannot override repeated stalls/user incident, while old incidents decay and do not permanently poison a recovered link.

**Primary source:** https://developer.android.com/develop/connectivity/network-ops/reading-network-state


## Scoring rule

B61-B80 deepen existing macro capabilities. The global denominator remains 80 until an explicit deduplication review demonstrates an independent product capability. This prevents research depth from artificially lowering or raising the macro completion percentage.

## Implementation already started

B61 is already being implemented on the active 0.8 branch: NetworkObserver is moving to callback-assembled snapshots and removing synchronous getNetworkCapabilities/getLinkProperties calls from callback flow. It remains below MACHINE_PROVEN until the exact new head passes CI.
