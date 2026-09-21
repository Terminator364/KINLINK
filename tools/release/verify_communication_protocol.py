#!/usr/bin/env python3
import json
from pathlib import Path

PROTOCOL = Path(".project-memory/COMMUNICATION_PROTOCOL.json")
ACTIVE = Path(".project-memory/ACTIVE_TRANCHE.json")
STATE_MACHINE = Path(".project-memory/COMMUNICATION_STATE_MACHINE.json")
LEDGER = Path(".project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

def main() -> int:
    p = json.loads(PROTOCOL.read_text(encoding="utf-8"))
    a = json.loads(ACTIVE.read_text(encoding="utf-8"))
    sm = json.loads(STATE_MACHINE.read_text(encoding="utf-8"))

    require(p.get("schema") == "kinlink.communication_protocol/3",
            "communication protocol: v3 schema missing")
    require(p.get("cadence_minutes") == 25,
            "communication protocol: cadence must be 25")
    require(p.get("primary_work_budget_minutes") == 22,
            "communication protocol: useful work must be 22 min")
    require(p.get("closeout_reserve_minutes") == 3,
            "communication protocol: reserve must be 3 min")
    require(p.get("normal_close_owner") == "PRIMARY_ASSISTANT",
            "communication protocol: primary normal-close owner missing")
    require(p.get("backup_earliest_offset_minutes") == 26,
            "communication protocol: backup must start after nominal window")
    require(p.get("hard_guard_offset_minutes") == 29,
            "communication protocol: hard guard offset mismatch")
    require("PREFER_END_REPLY_TO_START_THREAD" in p.get("threading", ""),
            "communication protocol: threaded END preference missing")
    require("STANDALONE_FALLBACK" in p.get("threading", ""),
            "communication protocol: verified standalone fallback missing")
    require("DELIVERY_KEY" in p.get("idempotency", ""),
            "communication protocol: delivery-key idempotency missing")

    backup = p.get("backup_sequence", [])
    require("SEARCH_GMAIL_THREAD_AND_DELIVERY_KEY_FOR_EXISTING_END" in backup,
            "communication protocol: backup must search before send")
    require("IF_EXISTING_END_FOUND_PERSIST_ACK_AND_CLOSE_WITHOUT_RESEND" in backup,
            "communication protocol: crash-window reconciliation missing")
    require("NEVER_USE_USER_VISIBLE_RECOVERY_OR_RATTRAPAGE_SUBJECT" in backup,
            "communication protocol: user-visible recovery wording fence missing")

    require(sm.get("schema") == "kinlink.communication_state_machine/3",
            "communication state machine: v3 schema missing")
    require(sm.get("useful_work_minutes") == 22,
            "communication state machine: 22-minute work budget missing")
    require(sm.get("normal_close_reserve_minutes") == 3,
            "communication state machine: 3-minute close reserve missing")
    require(sm.get("backup_earliest_offset_minutes") == 26,
            "communication state machine: backup preempts normal close")
    require(sm.get("hard_guard_offset_minutes") == 29,
            "communication state machine: hard guard mismatch")
    require("CLOSE_INTENT_PERSISTED" in sm.get("delivery_states", []),
            "communication state machine: close-intent state missing")
    require("after Gmail END send before GitHub receipt" in {
        x.get("window") for x in sm.get("crash_windows", [])
    }, "communication state machine: post-send crash window missing")

    require(a.get("schema") == "kinlink.active_tranche/3",
            "communication protocol: active tranche v3 missing")
    require(bool(a.get("delivery_key")),
            "communication protocol: delivery key missing")
    require(bool(a.get("gmail_start_message_id")),
            "communication protocol: START message ID missing")
    require(bool(a.get("gmail_start_thread_id")),
            "communication protocol: START thread ID missing")
    require(a.get("normal_close_owner") == "PRIMARY_ASSISTANT",
            "communication protocol: active primary close owner missing")
    require(a.get("useful_work_minutes") == 22,
            "communication protocol: active useful-work budget mismatch")
    require(a.get("normal_close_reserve_minutes") == 3,
            "communication protocol: active close reserve mismatch")
    require(a.get("backup_earliest_offset_minutes") == 26,
            "communication protocol: active backup preempts normal close")
    require(a.get("hard_guard_offset_minutes") == 29,
            "communication protocol: active hard guard mismatch")
    require(a.get("end_mail_reply_to_start") is True,
            "communication protocol: END threading preference missing")

    if a.get("status", "").startswith("CLOSED"):
        require(bool(a.get("gmail_end_message_id")),
                "communication protocol: closed tranche END message ID missing")
        require(a.get("end_mail_verified") is True,
                "communication protocol: closed tranche END verification missing")
        require(bool(a.get("close_owner")),
                "communication protocol: closed tranche close owner missing")

    rows = [
        json.loads(x)
        for x in LEDGER.read_text(encoding="utf-8").splitlines()
        if x.strip()
    ]
    require(rows, "communication ledger: empty")
    require(any(
        r.get("delivery_key") == a.get("delivery_key") and
        r.get("event") == "START_ACKNOWLEDGED"
        for r in rows
    ), "communication ledger: current START receipt missing")

    print("communication-protocol-v3: PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
