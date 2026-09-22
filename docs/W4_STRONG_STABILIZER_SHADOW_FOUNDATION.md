# W4 Strong Stabilizer — SHADOW gate foundation

This branch begins W4 without touching the staged KINLINK 0.8 field candidate and without enabling a VPN/TUN data plane.

## What is implemented

A pure deterministic policy gate now distinguishes three states:

- `DISABLED`: hard safety stop.
- `SHADOW_ONLY`: KINLINK may reason about eligibility but must not carry user traffic.
- `PER_APP_CANARY`: only the safety/consent gate is open; this does **not** mean production promotion.

Hard stops include Safe Mode, Emergency observation-only state, constrained resources, active handoff, unverified native-network escape, or observed harm.

Per-app canary remains blocked until LAN preservation, DNS safety, loop safety, MTU safety, IPv4, IPv6, user consent and metered-network policy are explicitly satisfied.

Production promotion remains separately gated by measurable benefit, resource budget, rollback, handoff regression, DNS, LAN, IPv4/IPv6, QUIC and native-escape evidence. Any observed harm forces rejection.

## Deliberate exclusions

- no `VpnService` declaration;
- no TUN creation;
- no routing ownership;
- no always-on VPN;
- no paid-data probe;
- no change to the staged 0.8 APK;
- no user installation request.

This is a machine-testable no-harm foundation for the later W4 data-plane implementation.
