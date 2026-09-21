# KINLINK — FULL CANONICAL PRODUCT SPECIFICATION A+B+C

Status: CANONICAL PRODUCT AMBITION — reconstructed from the original preconception, deepened research, and accumulated field/user feedback.

## A — Original product intent recovered

A is not the recent 0.7.x traceability table. It is the original 20 September 2026 product design preserved in Git history and Library artifacts.

Recovered anchor: `c565b6f1164d327caaa17e1de47d1cf48ed3c885`.

Original mission: an install-once, low-overhead Android connectivity autopilot for unstable, constrained and metered networks. It must improve perceived stability without inventing bandwidth; prefer trusted Wi‑Fi; protect paid mobile data; diagnose radio/LAN/router/ISP/DNS/IPv4/IPv6/remote-service failures separately; preserve LAN during WAN loss; fail open; learn locally in SHADOW before aggressive autonomy; and produce machine-readable evidence.

Original architecture included:
- Control Plane: UI, Context Engine, Policy Engine, Connectivity Truth Engine, Personal Network Twin, Mobile Vault, Carrier Wallet, Adaptive Learner, report/update policy.
- Network Data Plane: optional VpnService/TUN, flow classification, DNS resilience, safe per-flow routing, shaping/admission controls, watchdog and immediate fail-open teardown.
- Two-process isolation between control and data plane.
- Lite Observer and a separately qualified Full Autopilot.
- Five main user surfaces: Home, Diagnostic, This week, Mobile Vault, Settings, plus Emergency action.
- Full structured diagnostics and a local weekly report.
- Conservative/Balanced/Maximum Stability profiles.
- One provisioning flow then normal autopilot use.

A is now immutable historical intent. It cannot be deleted merely because recent versions implemented a smaller safe slice.

## B — Concrete expansion beyond A

B does not mean adding vague ideas. It turns A into a stronger achievable architecture using current Android capabilities and field-realistic constraints.

### B1 — Native zero-extra-traffic diagnostics
Integrate Android `ConnectivityDiagnosticsManager` to consume platform connectivity reports and suspected data-stall events. This gives additional DNS/TCP stall evidence without KINLINK generating a speed test.

### B2 — Better mobile-data accounting
Keep lightweight TrafficStats for immediate low-cost observation. Add an optional, user-granted `NetworkStatsManager` path for richer daily/user-wide accounting where Usage Access is granted. Run expensive queries off the main thread and never pretend they are operator balance.

### B3 — Experience truth instead of bandwidth theatre
Use explicit user report, interruption/slow-link history and platform stall evidence before passive Android bandwidth estimates. The first-hop estimate is useful technical evidence but cannot become a fake experienced-quality score.

### B4 — Strong Stabilizer as a separate product subsystem
The strong stabilizer remains required, but only behind explicit VPN consent and fresh proof. Build it as a separate data plane with:
- crash isolation;
- watchdog/fail-open;
- local packet forwarding only;
- privacy-safe flow metadata;
- DNS resilience;
- optional allowed/disallowed-app rollout;
- meteredness inheritance/protection;
- underlying-network awareness;
- IPv4/IPv6/QUIC regression coverage;
- immediate rollback to Android-native networking.

### B5 — Shadow experimentation before autonomy
For every adaptive policy: champion/challenger in SHADOW, evidence window, benefit/cost score, negative controls, rollback, and promotion only if benefit survives counter-audit.

### B6 — Product-level experience SLOs
Track concrete experience indicators instead of one opaque quality score:
- interruption count/duration;
- platform data stalls;
- slow-link episodes;
- validated-but-degraded windows;
- handoff failures;
- user incident reports;
- intervention benefit/neutral/harmful/unknown;
- relapse rate;
- battery/RAM/thermal cost.

### B7 — Carrier/data protection progression
Manual plan/expiry and reserve envelopes first; optional carrier adapters only when they can be reliable and rate-limited. Parser failure always becomes UNKNOWN.

### B8 — Target-device visual proof
Rendered emulator matrices remain necessary but insufficient. Dialogs, expanded technical detail, large font, safe mode, resource pressure, mobile/Wi‑Fi/offline states and exact target-device screenshots are release evidence.

Research basis:
- https://developer.android.com/reference/android/net/ConnectivityDiagnosticsManager
- https://developer.android.com/reference/android/app/usage/NetworkStatsManager
- https://developer.android.com/reference/android/net/VpnService
- https://developer.android.com/reference/android/net/VpnService.Builder
- https://developer.android.com/develop/connectivity/network-ops/reading-network-state
- https://developer.android.com/develop/connectivity/vpn

## C — Accumulated user/field evolution

C is the durable product learning layer:
- no micro-beta installation chain;
- upstream simulation/CI before human testing;
- target screenshots auto-counter-audited;
- Gmail START/END crash-safe communication;
- continuity across conversations and network loss;
- one unambiguous human APK path;
- user experience outranks optimistic Android estimates;
- MB/GB human units;
- no fake quality score;
- improvement requires evidence, not metric refresh theatre;
- anti-repeat/escalation after ineffective actions;
- stronger visual hierarchy and progressive disclosure;
- field findings become regression tests;
- the next delivered version must be a coherent integrated advance, not another tiny patch.

## Canonical completion measurement

The old 56% number is superseded because it measured a recent traceability subset.

The canonical ledger now contains **80 deduplicated macro capabilities** spanning A+B+C. Each macro capability has one maturity stage so many small safety checks cannot inflate one missing major capability.

Current A+B+C macro maturity: **44.8%**.

This is a conservative management score, not a probability and not a marketing percentage.

The full machine-readable ledger is:
`.project-memory/FULL_REQUIREMENTS_LEDGER.json`

The full scorecard is:
`.project-memory/PRODUCT_COMPLETION_SCORECARD.json`

## Product direction from this point

The active 0.8 line is no longer an “UI correction release”. It is the first integrated re-convergence toward the full A+B+C product.

No field installation is requested until the integrated batch has:
1. exact-head machine gates;
2. platform diagnostics and experience-truth improvements;
3. complete visual matrix including dialogs/details;
4. manual counter-audit of rendered proof;
5. stable signing + Drive readback;
6. target-device field plan.

Major missing A-value (Strong Stabilizer, full Mobile Vault/Carrier Wallet, learner, full screens/reports) remains explicitly visible and scheduled rather than disappearing from the specification.
