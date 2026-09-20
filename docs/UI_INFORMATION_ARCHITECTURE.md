# UI Information Architecture

## Screen 1 — Home / Cockpit
Show only:
- Autopilot state
- current network context
- plain-language health
- Mobile Vault state + data spend
- battery-impact label
- last automatic fix or current issue
- actions: Diagnostic, This week, Emergency

## Screen 2 — Diagnostic
A vertical path:
Phone -> Radio -> LAN/Router -> ISP -> DNS/IP -> Internet -> Remote service
Each stage is Healthy / Degraded / Down / Unknown with confidence.

## Screen 3 — This week
Verifiable counts only:
- brownouts/outages
- automatic recoveries
- unresolved failures
- data used by transport/class
- interventions beneficial/neutral/harmful/unknown
- rollback count
- battery/thermal impact summary

## Screen 4 — Mobile Vault
- plan size / expiry
- remaining estimate and source
- reserve
- rescue allowance
- critical allowance
- carrier-adapter status
No complicated policy matrix in the normal view.

## Screen 5 — Settings
Top-level settings only:
- Autopilot: Conservative / Balanced / Maximum Stability
- Home Wi-Fi recognition
- Mobile budget
- Carrier Wallet opt-in
- Always-on Autopilot status
- Diagnostic privacy
Advanced technical settings are hidden behind an expert section.

## Visual rules
- blue-dominant visual identity
- dark/light adaptive system theme
- cards with restrained elevation
- status icon + short text, not color alone
- no fake percentages for open-ended network quality
- charts only where history materially helps
- animation kept subtle to avoid battery/UI overhead
