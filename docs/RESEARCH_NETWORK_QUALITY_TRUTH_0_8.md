# KINLINK 0.8 — research basis for truthful network UX

Date: 2026-09-21

## Android framework truth

Official Android NetworkCapabilities documentation:
https://developer.android.com/reference/android/net/NetworkCapabilities

Key design consequence:
- getLinkDownstreamBandwidthKbps() and getLinkUpstreamBandwidthKbps() are **estimated first-hop transport bandwidth**.
- They are not an Internet speed test and not end-to-end experienced throughput.
- Therefore KINLINK must not convert them into a user-facing “100/100” or “Confortable” claim.

Official Android TrafficStats documentation:
https://developer.android.com/reference/android/net/TrafficStats

Key design consequence:
- getMobileRxBytes()/getMobileTxBytes() report bytes across mobile networks since boot.
- This is device-wide mobile traffic, not KINLINK-only traffic and not operator wallet balance.
- Human UI should use concise decimal MB/GB while retaining the scope disclaimer.

Official Android accessibility guidance:
https://developer.android.com/guide/topics/ui/accessibility/apps

Key design consequence:
- interactive touch targets should be at least 48dp.
- KINLINK keeps this gate while also adding real target-phone semantic visual review.

## Community corroboration

Stack Overflow discussion:
https://stackoverflow.com/a/31696452/11278807

The explanation matches Android's API contract: “first hop” is link capability, not the bandwidth available through the routed Internet path to an endpoint.

## Product rule derived from research

Android framework signals are **technical evidence**, not the user's experienced quality.

User-facing truth order:
1. explicit user problem report;
2. recent reliability/slow-link history;
3. current Android validation/congestion/suspension evidence;
4. Android passive bandwidth estimates;
5. internal numeric score only in technical diagnostics, never as a promise of experienced quality.

Any “improvement” label requires durable sustained evidence. A metrics refresh alone is observation refresh, not throughput improvement.
