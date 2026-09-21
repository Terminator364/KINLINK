#!/usr/bin/env python3
import json
from pathlib import Path

PATH = Path(".project-memory/COMMUNICATION_PROTOCOL.json")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

def main() -> int:
    data = json.loads(PATH.read_text(encoding="utf-8"))
    require(data.get("project") == "KINLINK", "communication protocol: project mismatch")
    require(data.get("cadence_minutes") == 25, "communication protocol: cadence must be 25 minutes")
    require(
        data.get("start_sequence") == [
            "SEND_GMAIL_START",
            "APPLY_GMAIL_LABEL_KINLINK",
            "BEGIN_WORK",
        ],
        "communication protocol: Gmail must precede work",
    )
    require(
        data.get("end_sequence") == [
            "SEND_COMPLETE_GMAIL_END_REPORT",
            "APPLY_GMAIL_LABEL_KINLINK",
            "VERIFY_SEND_RETURNED_MESSAGE_ID",
            "ONLY_THEN_REPLY_IN_CHAT_APP",
        ],
        "communication protocol: end mail must precede app reply",
    )
    require(data.get("no_app_reply_before_end_mail") is True,
            "communication protocol: app-reply fence missing")
    require(data.get("end_mail_required_even_if_user_sends_intermediate_feedback") is True,
            "communication protocol: feedback must not bypass end mail")
    require(data.get("gmail_label") == "KINLINK",
            "communication protocol: Gmail label mismatch")
    require("NEVER_GMAIL_ATTACHMENT" in data.get("apk_delivery", ""),
            "communication protocol: APK email ban missing")
    require(
        data.get("only_early_stop") == "TRUE_HUMAN_GATE_OBJECTIVELY_REQUIRED_TO_CONTINUE",
        "communication protocol: human-gate exception widened",
    )
    active_path = Path(".project-memory/ACTIVE_TRANCHE.json")
    require(active_path.exists(), "communication protocol: ACTIVE_TRANCHE.json missing")
    active = json.loads(active_path.read_text(encoding="utf-8"))
    require(active.get("cadence_minutes") == 25,
            "communication protocol: active tranche cadence mismatch")
    require(bool(active.get("gmail_start_message_id")),
            "communication protocol: start Gmail message ID missing")
    require(active.get("end_mail_required_before_app_reply") is True,
            "communication protocol: active tranche end-mail fence missing")
    print("communication-protocol: PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
