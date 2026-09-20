# KINLINK implementation plan

## Proven
- M0: repository, CI, evidence discipline.
- M1: observer, LAN/WAN separation, ledger, cockpit, export.
- M2: persistent signing, Android 15/16 FGS hardening, explicit Wi-Fi action.
- M3: multi-signal arbitration and anti-false-negative regression.
- M4: Mobile Vault.
- M6-safe: stability window, anti-flapping and persistent profiles.
- RC2: automatic bounded Wi-Fi recovery, rate limiting, metered guard, resource guard, reboot/update restart and action receipts.

## Current promoted candidate

0.5.0-rc2 passed CI, stable signing and Drive byte-for-byte readback. Field verification is the next gate.

## M5 — optional strong stabilizer

Still gated:
- VpnService/TUN prototype;
- DNS resilience/cache benchmark;
- fail-open heartbeat/watchdog runtime;
- RAM/battery/thermal benchmark;
- added-latency benchmark;
- rollback test.

M5 must automatically disable itself if it does not demonstrate a measurable benefit or if it risks creating a worse failure mode.
