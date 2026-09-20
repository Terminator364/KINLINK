# NEXT_UNCOMMITTED_ACTION

## Last proven checkpoint

- Commit `336ede392ce7616b991a227e940b05cfc101a60a`: offline-only signing bootstrap and documentation.
- GitHub Actions run `35522715744`: **PASS** — unit tests and CI debug APK build.
- The first installed M0 APK is only an observer bootstrap. Its field screenshot confirmed that the product needs clearer human diagnostics before another phone action.

## Next action

Implement the **minimum field diagnostic package**:
1. bounded local event summary from the existing ledger;
2. user-initiated diagnostic export with no SSID, SIM identifier, token, payload, or password;
3. human-readable explanation of LAN/WAN, Wi-Fi/mobile, offline and metered states;
4. tests and CI build;
5. request one focused field check only after the next APK has a concrete diagnostic benefit.

Release signing remains offline-only and is deferred until a viable field candidate is ready. Do not publish a CI debug APK as an update.
