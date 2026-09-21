#!/usr/bin/env python3
import json
from pathlib import Path

PROTOCOL = Path(".project-memory/COMMUNICATION_PROTOCOL.json")
ACTIVE = Path(".project-memory/ACTIVE_TRANCHE.json")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

def main() -> int:
    data = json.loads(PROTOCOL.read_text(encoding="utf-8"))
    active = json.loads(ACTIVE.read_text(encoding="utf-8"))

    require(data.get("project") == "KINLINK", "communication protocol: project mismatch")
    require(data.get("cadence_minutes") == 25, "communication protocol: cadence must be 25 minutes")
    require(data.get("primary_work_budget_minutes") == 20,
            "communication protocol: 20-minute primary work budget missing")
    require(data.get("closeout_reserve_minutes") == 5,
            "communication protocol: 5-minute closeout reserve missing")
    require(data.get("max_mutation_groups_before_forced_closeout") == 12,
            "communication protocol: mutation budget missing")
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
    require("NEVER_GMAIL_ATTACHMENT" in data.get("apk_delivery", ""),
            "communication protocol: APK email ban missing")

    require(active.get("cadence_minutes") == 25,
            "communication protocol: active tranche cadence mismatch")
    require(bool(active.get("gmail_start_message_id")),
            "communication protocol: active tranche start Gmail message ID missing")
    require(active.get("end_mail_required_before_app_reply") is True,
            "communication protocol: active tranche end-mail fence missing")
    require(active.get("closeout_reserve_minutes") == 5,
            "communication protocol: active tranche closeout reserve missing")
    require(active.get("max_mutation_groups_before_forced_closeout") == 12,
            "communication protocol: active tranche mutation budget missing")

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
