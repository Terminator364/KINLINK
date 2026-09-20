# Component Contracts

## Core models
`NetworkTruth`
- transport
- context
- localLinkState
- lanState
- internetState
- failureDomain
- budgetState
- batteryState
- thermalState
- confidence
- evidenceIds

`NetworkObservation`
- source
- timestamp
- networkHandle
- metrics
- privacyClass

`PolicyDecision`
- action
- reasonCode
- confidence
- expectedBenefit
- dataCostCeilingBytes
- energyClass
- rollbackPlan
- expiry

## Components

### ObserverCore
Consumes Android callbacks and emits normalized observations. No policy mutations.

### ConnectivityTruthEngine
Reduces observations into a coherent current state. It must preserve UNKNOWN when evidence is insufficient.

### ContextEngine
Recognizes HOME_WIFI / TRUSTED_WIFI / UNKNOWN_WIFI / MOBILE_RESILIENT / CAPTIVE_PORTAL without requiring continuous GPS.

### IntentClassifier
Classifies flows/apps into CONTROL, INTERACTIVE, REALTIME_LOCAL, BULK, STREAM, BACKGROUND, UNKNOWN. It never inspects encrypted payload content.

### MobileVault
Hard budget enforcement. Has veto authority over any learner/policy request that would exceed an envelope.

### CarrierWallet
Maintains balance estimates and optional operator adapters. Never blocks networking if carrier parsing fails.

### PolicyEngine
Deterministic decision layer. Learner recommendations are inputs, not commands.

### AdaptiveLearner
Starts in SHADOW. Generates recommendations and confidence only.

### ActionExecutor
Executes reversible actions, records before/after evidence, and triggers rollback when policy conditions fail.

### TelemetryLedger
Append-oriented durable evidence with bounded retention and privacy filtering.

### DiagnosticExporter
Creates shareable sanitized reports.

### FailOpenSupervisor
Independent watchdog for the data plane. Networking safety outranks optimization quality.
