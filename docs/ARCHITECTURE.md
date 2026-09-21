# Architecture

## Process model

### Process A — Control Plane
Responsibilities:
- UI/cockpit
- Context Engine
- Policy Engine
- Connectivity Truth Engine
- Personal Network Twin
- Mobile Vault / Carrier Wallet policy
- learner and shadow evaluation
- report generation
- update policy

### Process B — Network Data Plane
Responsibilities:
- optional Android VpnService/TUN
- flow classification
- DNS resilience backend
- safe per-flow routing policy
- bounded shaping/admission controls
- watchdog heartbeat
- immediate fail-open teardown

The two processes communicate through a narrow versioned IPC contract. A crash in Process A must not kill Process B. A failure in Process B must trigger clean teardown and return networking to Android.

## Always-on observer
The Observer Core relies primarily on Android callbacks, not polling:
- ConnectivityManager callbacks
- NetworkCapabilities / LinkProperties
- Wi-Fi information where permission permits
- Telephony callbacks where permission permits
- ConnectivityDiagnostics events
- battery and thermal events

## Stabilizer activation
The data plane is not allowed to perform aggressive work simply because it is installed. Policies can activate stronger mechanisms only when:
- an anomaly exists,
- the action is allowed by hard constraints,
- expected benefit exceeds cost,
- confidence is above the action threshold.

## Local-first design
No cloud is required for normal operation. Diagnostic exports are user-initiated or sent through a future trusted BCP bridge.


### Mobile Assist Level 1

Runs in the existing control plane and does not require VpnService.

Inputs:
- active Android transport/validation;
- passive bandwidth estimates;
- NOT_CONGESTED / NOT_SUSPENDED capabilities;
- Mobile Vault budget state;
- battery/thermal/low-memory guard;
- recent Mobile Assist receipts.

Permitted executor action:
- requestBandwidthUpdate on the still-active validated cellular network.

The action is metadata refresh only. It does not create a test flow, own routing, request a new cellular network or alter Android validation state.

Manual fallback:
- explicit user tap may open Android's Internet connectivity panel when the current cellular network is unvalidated/suspended.

### Strong Mobile Stabilizer Level 2

Remains in the separately gated Network Data Plane. See `docs/MOBILE_RESILIENCE.md`.
