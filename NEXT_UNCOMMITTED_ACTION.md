# NEXT_UNCOMMITTED_ACTION

## Installed field build

- KINLINK 0.7.1 / versionCode 9 is now installed.
- Exact signed SHA-256:
  `0c2210acf29e18926634fc1d8b23b156cce153e1b536bb62b87615199dca6eae`
- Installation is confirmed by the user and the visible 0.7.1 qualification label in target-phone screenshots.
- Keep this exact build frozen during Stage B.

## Stage B still required

Collect on exact versionCode 9:
- core self-test PASS;
- observer callback self-test PASS;
- Wi-Fi -> validated cellular handoff;
- cellular -> Wi-Fi return;
- >=30-minute resource qualification.

Do **not** replace canonical INSTALLER / 0.6.0-rc3 until those gates pass.

## 0.7.2 engineering line

All new UX / continuous-improvement work is isolated on:
`dev/0.7.2-continuous-improvement`

Current goals:
- compact status/action/proof cockpit;
- truthful passive 0..100 quality index;
- sustained Mobile Assist evidence instead of one instant;
- explicit transient relapse and later relapse detection;
- event-driven continuous care with no speedtest and no repeating polling loop;
- lower CPU/SQLite work on healthy callbacks;
- stronger thermal/RAM/battery protection;
- no 0.7.2 installation until exact-head design-lint + Android CI + counter-audit pass.

## Communication

- Gmail start first.
- 25-minute coherent tranche.
- Durable checkpoint.
- Complete Gmail end report before any app reply.
- End Gmail message ID must be persisted.
- APK only via KINLINK Drive.

## Resume

`KINLINKGO`
