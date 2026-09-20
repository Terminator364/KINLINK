# KINLINK

KINLINK is an Android connectivity autopilot designed for constrained, unstable, and metered networks.

Core goals:
- improve perceived network stability without pretending to create bandwidth;
- prefer trusted Wi-Fi when useful;
- protect paid mobile data with hard budgets and explicit policy;
- diagnose radio/LAN/router/ISP/DNS/IPv4/IPv6/remote-service failures separately;
- preserve local LAN even when WAN is down;
- fail open if KINLINK itself becomes unhealthy;
- learn locally in shadow mode before taking aggressive actions;
- produce machine-readable diagnostics for evidence-driven updates.

Status: M1 Observer Core implemented, awaiting Android build and field gates.

The first field candidate observes Android's existing connectivity state. It never
starts a speed test, changes a network setting, enables cellular data, or installs
a VPN/TUN data plane. Its failure therefore leaves Android networking untouched.
