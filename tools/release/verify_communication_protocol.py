#!/usr/bin/env python3
import json
import os
from pathlib import Path

PROTOCOL = Path(".project-memory/COMMUNICATION_PROTOCOL.json")
ACTIVE = Path(".project-memory/ACTIVE_TRANCHE.json")
STATE_MACHINE = Path(".project-memory/COMMUNICATION_STATE_MACHINE.json")
DELIVERY_LEDGER = Path(".project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

def main() -> int:
    data = json.loads(PROTOCOL.read_text(encoding="utf-8"))
    active = json.loads(ACTIVE.read_text(encoding="utf-8"))
    machine = json.loads(STATE_MACHINE.read_text(encoding="utf-8"))
    ledger_lines = [
        line for line in DELIVERY_LEDGER.read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]

    require(data.get("project") == "KINLINK", "communication protocol: project mismatch")
    require(data.get("cadence_minutes") == 25, "communication protocol: cadence must be 25 minutes")
    require(data.get("primary_work_budget_minutes") == 22,
            "communication protocol: 22-minute primary work budget missing")
    require(data.get("closeout_reserve_minutes") == 3,
            "communication protocol: 3-minute closeout reserve missing")
    mutation_budget = data.get("max_mutation_groups_before_forced_closeout")
    require(isinstance(mutation_budget, int) and mutation_budget > 0,
            "communication protocol: mutation budget missing")
    imported = data.get("bcp_imported_principles", [])
    require(
        "GMAIL_START_PROVIDER_ACK_BEFORE_SUBSTANTIVE_WORK" in imported
        and "ONE_SHOT_END_WATCHDOG_ARMED_AT_TRANCHE_START" in imported
        and "NO_CHATGPT_POINTER_BEFORE_PROVIDER_ACK_END" in imported,
        "communication protocol: BCP provider-ack/watchdog principles missing",
    )
    require(
        data.get("start_sequence", [])[0] == "REPAIR_ANY_UNCLOSED_PRIOR_TRANCHE",
        "communication protocol: prior unclosed tranche repair is not first",
    )
    require(
        "ENTER_FORCED_CLOSEOUT_RESERVE" in data.get("end_sequence", [])
        and "STOP_NEW_TECHNICAL_MUTATIONS" in data.get("end_sequence", []),
        "communication protocol: forced closeout reserve missing",
    )
    guard = data.get("system_interruption_guard", {})
    require(
        guard.get("required_first_action") == "SEND_MISSING_GMAIL_END_REPORT",
        "communication protocol: interruption recovery first action mismatch",
    )
    require(guard.get("never_reply_in_app_with_unclosed_tranche") is True,
            "communication protocol: app-reply interruption fence missing")
    require(data.get("no_app_reply_before_end_mail") is True,
            "communication protocol: app-reply fence missing")
    require(data.get("end_mail_required_even_if_user_sends_intermediate_feedback") is True,
            "communication protocol: feedback must not bypass end mail")
    require(data.get("gmail_label") == "KINLINK",
            "communication protocol: Gmail label mismatch")
    states = data.get("delivery_state_machine", [])
    require(
        states[:3] == ["START_REQUIRED", "START_PROVIDER_ACKED", "WORKING"]
        and states[-2:] == ["END_PROVIDER_ACKED", "CLOSED"],
        "communication protocol: delivery state machine incomplete",
    )
    require("NEVER_GMAIL_ATTACHMENT" in data.get("apk_delivery", ""),
            "communication protocol: APK email ban missing")

    require(machine.get("cadence_minutes") == 25,
            "communication protocol: state-machine cadence mismatch")
    require(machine.get("useful_work_minutes") == 22,
            "communication protocol: state-machine useful-work budget mismatch")
    require(machine.get("closeout_reserve_minutes") == 3,
            "communication protocol: state-machine closeout reserve mismatch")
    require(
        machine.get("watchdogs", {}).get("closeout", {}).get("trigger_offset_minutes") == 22
        and machine.get("watchdogs", {}).get("hard_end_guard", {}).get("trigger_offset_minutes") == 25,
        "communication protocol: state-machine watchdog offsets missing",
    )
    require(len(ledger_lines) >= 1,
            "communication protocol: delivery ledger is empty")
    # ACTIVE_TRANCHE is canonical runtime delivery state. A pull-request synthetic
    # merge must validate the static communication contract without pretending
    # its branch snapshot owns the live Gmail handshake.
    event_name = os.environ.get("GITHUB_EVENT_NAME", "")
    if event_name != "pull_request":
        require(active.get("work_state") in machine.get("work_states", []),
                "communication protocol: active work state is invalid")
        require(active.get("delivery_state") in machine.get("delivery_states", []),
                "communication protocol: active delivery state is invalid")
        require(active.get("start_provider_ack") is True,
                "communication protocol: provider START acknowledgement missing")
        watchdogs = active.get("watchdogs", {})
        require(
            watchdogs.get("closeout_watchdog_armed") is True
            and watchdogs.get("hard_end_guard_armed") is True
            and watchdogs.get("closeout_trigger_minutes") == 22
            and watchdogs.get("hard_end_trigger_minutes") == 25,
            "communication protocol: active watchdog pair is not armed",
        )
        require(active.get("cadence_minutes") == data.get("cadence_minutes"),
                "communication protocol: active tranche cadence mismatch")
        require(bool(active.get("gmail_start_message_id")),
                "communication protocol: active tranche start Gmail message ID missing")
        require(active.get("end_mail_required_before_app_reply") is True,
                "communication protocol: active tranche end-mail fence missing")
        require(
            active.get("closeout_reserve_minutes") ==
                data.get("closeout_reserve_minutes"),
            "communication protocol: active tranche closeout reserve mismatch",
        )
        require(
            active.get("max_mutation_groups_before_forced_closeout") ==
                mutation_budget,
            "communication protocol: active tranche mutation budget mismatch",
        )

        if active.get("status", "").startswith("CLOSED"):
            require(active.get("forced_closeout_entered") is True,
                    "communication protocol: closed tranche missing forced closeout marker")
            require(bool(active.get("gmail_end_message_id")),
                    "communication protocol: closed tranche end Gmail message ID missing")
            require(active.get("end_mail_verified") is True,
                    "communication protocol: closed tranche end-mail verification missing")

    print("communication-protocol: PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
