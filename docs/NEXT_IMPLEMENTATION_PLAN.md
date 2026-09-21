# KINLINK implementation plan

## Proven baseline

- M0: repository, CI, evidence discipline.
- M1: observer, LAN/WAN separation, ledger, cockpit, export.
- M2: persistent signing, Android 15/16 foreground-service hardening, explicit Wi-Fi action.
- M3: multi-signal arbitration and anti-false-negative regression.
- M4: Mobile Vault / paid-data protection.
- M6-safe: stability window, anti-flapping and persistent profiles.
- RC3: hard fail-open Wi-Fi/mobile handoff, persistent safe mode, watchdog, restart-storm degradation, post-handoff settling, stable signing and canonical Drive promotion.
- RC3 field gate: Wi-Fi -> cellular -> Wi-Fi PASS; original mobile-block symptom NOT REPRODUCED.

## Immutable field baseline

- Version: 0.6.0-rc3
- versionCode: 7
- Status: installed + field-validated
- Canonical Drive artifact remains the rollback/reference field build.

## Active development line

- Version: 0.7.1
- versionCode: 9
- The previous signed Stage A artifact was superseded before field delivery when the mobile-resilience requirement was restored.
- No human install is allowed until the Mobile Assist batch passes fresh exact-head CI, audit, signing and Drive readback.

Integrated in 0.7.0-dev:
- truthful mobile-counter semantics;
- 1.5 s transient handoff-loss UI settling;
- passive Android bandwidth estimates;
- passive quality classes and low-noise quality history;
- quality-aware hero state and Autopilot;
- 3-observation hysteresis before low-quality recovery refresh;
- Wi-Fi micro-probe responsiveness classification;
- immediate probe abort if handoff starts;
- metered Wi-Fi zero-HTTP manual optimization;
- recovery effectiveness receipts;
- anti-repeat ineffective recovery;
- passive failure-cause classification;
- DNS metadata observation without DNS override;
- passive interruption duration/severity;
- cause-aware guidance and notification;
- qualitative session-health synthesis.

## Next material milestones

1. Finish CI on current 0.7.0-dev head.
2. Harden longitudinal diagnostics:
   - cause history;
   - interruption history;
   - recovery effectiveness history;
   - privacy-safe export.
3. Add first-launch/post-update self-test for:
   - database migration;
   - observer callbacks;
   - fail-open policy;
   - safe-mode persistence;
   - no forbidden routing/network ownership API.
4. Run resource qualification:
   - RAM delta;
   - battery impact;
   - wakeups/background churn;
   - added UI/network latency.
5. Run rollback/update qualification from RC3 -> next candidate and back to last-known-good where Android allows.
6. Only then decide whether 0.7.0-dev becomes the next consolidated signed candidate.

## Strong stabilizer / VPN-TUN work

Still gated and disabled in production.

No VpnService/TUN capability may be enabled or promoted until separate evidence proves:
- measurable benefit;
- bounded RAM/battery/latency cost;
- correct DNS behavior;
- watchdog teardown;
- fail-open behavior;
- rollback;
- no regression of Wi-Fi/mobile handoff.

Strong recovery must automatically disable itself if it does not demonstrate benefit or risks creating a worse failure mode.


## Mobile-resilience restoration

Current engineering priority:
1. finish Mobile Assist Level 1 in 0.7.1;
2. keep automatic cellular traffic at zero;
3. classify passive mobile low-capacity / congestion / suspension;
4. rate-limit zero-probe Android metric refresh;
5. measure whether Mobile Assist actually helps and stop repeating ineffective work;
6. expose one user-invoked Android connectivity-panel fallback;
7. rerun full exact-head CI/counter-audit/signing;
8. only then stage one consolidated candidate.

The strong VpnService/TUN Mobile Stabilizer remains the next separately gated architecture, not a shortcut around evidence.
