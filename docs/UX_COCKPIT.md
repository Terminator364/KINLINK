# UX Cockpit

## Home screen
Primary language: simple human-readable French.
Visual direction: blue-dominant, calm, high contrast, card-based, minimal clutter.

Primary card:
- AUTOPILOT status
- current context (Home Wi-Fi / Mobile / Unknown Wi-Fi)
- health label
- latency summary
- Mobile Vault state
- current mobile-data spend
- battery impact label

Primary actions:
- Diagnostic
- This week
- Emergency

## Progressive disclosure
Technical details (RSSI, BSSID hash, gateway RTT, DNS timings, IPv4/IPv6, state transitions) live behind a Details screen.

## Explainability
Every automatic action exposes:
- Why?
- What did KINLINK do?
- Did it help?

## Notification philosophy
- normal operation: quiet
- auto-recovery: logged, usually silent
- action needed: notification
- budget risk / exhausted: high visibility
- one persistent foreground-service notification when Android requires it
