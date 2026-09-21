# KINLINK continuity

## Canonical recovery command

`KINLINKGO`

## Resume procedure

1. Open `Terminator364/KINLINK`, branch `main`.
2. Reload in order:
   - `project_state.json`
   - `NEXT_UNCOMMITTED_ACTION.md`
   - `BUILD_STATUS.md`
   - `docs/CANONICAL_SPEC.md`
   - `docs/KINLINK_CONTINUITY.md`
   - `.project-memory/COMMUNICATION_PROTOCOL.json`
   - `.project-memory/ACTIVE_TRANCHE.json`
3. Read latest commits and exact relevant CI runs.
4. Resume from `next_uncommitted_action`; never restart M0/M1.
5. Preserve the exact field-candidate freeze until Stage B is resolved.

## Communication protocol — mandatory

Current cadence: **25 minutes**.

Start:
1. send Gmail start;
2. apply Gmail label `KINLINK`;
3. persist `gmail_start_message_id` in `.project-memory/ACTIVE_TRANCHE.json`;
4. begin work.

During tranche:
- intermediate feedback is queued/integrated without restarting the tranche;
- network/tool interruption creates a durable checkpoint and resumes from it;
- early stop is allowed only for a true human gate objectively required to continue.

End:
1. persist final technical checkpoint;
2. send complete Gmail end report;
3. apply Gmail label `KINLINK`;
4. verify returned Gmail message ID;
5. persist tranche CLOSED with end message ID;
6. only then reply in the ChatGPT app with Gmail + Kinshasa date/time.

APK distribution:
- never via Gmail attachment;
- Drive KINLINK only.

## Durable product state

Canonical rollback:
- `0.6.0-rc3` / versionCode 7;
- `KINLINK/INSTALLER/KINLINK_LATEST.apk` unchanged.

Installed phone:
- `0.7.0` / versionCode 8;
- usable but promotion-invalidated.

Current consolidated field candidate:
- `0.7.1` / versionCode 9;
- exact functional source: `a15a784ab51ae048e37ae31c998092ad2cd3f03c`;
- design-lint `35594766801`: PASS;
- Android candidate `35594766821`: PASS;
- signed SHA-256: `0c2210acf29e18926634fc1d8b23b156cce153e1b536bb62b87615199dca6eae`;
- signer cert: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`;
- Drive file ID: `1hixAB1tKY8G3u-ajD16PJ6kQ-lznNaV1`;
- Drive readback: PASS;
- frozen for field qualification.

Mobile Assist Level 1:
- zero hidden cellular probe;
- passive mobile degradation diagnosis;
- bounded Android metric refresh;
- cooldown/hourly/resource/budget/weak-radio/anti-repeat gates;
- explicit system-panel fallback;
- mobile reliability evidence and diagnostics.

Strong VpnService/TUN stabilizer remains gated future work until proof of measurable benefit + RAM/battery/latency/DNS/watchdog/rollback safety.

## Next true action

One in-place installation of the exact 0.7.1 Drive field candidate is now required.

After installation:
- collect versionCode 9 self-test;
- collect Wi-Fi -> mobile and mobile -> Wi-Fi handoff;
- collect >=30-minute resource qualification;
- promote to canonical INSTALLER only after Stage B PASS.

## Short code

`KINLINKGO`
