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
