#!/usr/bin/env python3
import json
from pathlib import Path

def req(c,m):
    if not c: raise SystemExit(m)

p=json.loads(Path(".project-memory/COMMUNICATION_PROTOCOL.json").read_text())
sm=json.loads(Path(".project-memory/COMMUNICATION_STATE_MACHINE.json").read_text())
a=json.loads(Path(".project-memory/ACTIVE_TRANCHE.json").read_text())
ds=json.loads(Path(".project-memory/DELIVERY_SECURITY_POLICY.json").read_text())
rows=[json.loads(x) for x in Path(".project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl").read_text().splitlines() if x.strip()]
req(p.get("schema")=="kinlink.communication_protocol/7","communication protocol v7 missing")
req(sm.get("schema")=="kinlink.communication_state_machine/7","communication state machine v7 missing")
req(a.get("schema") in {"kinlink.active_tranche/6","kinlink.active_tranche/7"},"active tranche v5 missing")
req(ds.get("schema")=="kinlink.delivery_security_policy/1","delivery security policy missing")
req(p.get("cadence_mode")=="MILESTONE_BASED_NO_FIXED_DURATION" and p.get("cadence_minutes") is None and p.get("primary_work_budget_minutes") is None and p.get("closeout_reserve_minutes") is None,"milestone timing rule broken")
req(p.get("final_reply_gate",{}).get("required_delivery_state")=="END_ACKNOWLEDGED_OR_CLOSED","final reply gate missing")
req(p.get("human_install_surface",{}).get("max_apk_cardinality")==1,"single installer gate missing")
req(p.get("security_continuity",{}).get("platform_security_checks")=="OBEY_AND_NEVER_BYPASS","security continuity missing")
req(bool(a.get("delivery_key")),"delivery key missing")
req(bool(a.get("gmail_start_message_id")) and bool(a.get("gmail_start_thread_id")),"START ACK missing")
req(any(r.get("delivery_key")==a.get("delivery_key") and r.get("event")=="START_ACKNOWLEDGED" for r in rows),"ledger START missing")
if a.get("status")=="CLOSED":
    req(bool(a.get("gmail_end_message_id")) and a.get("end_mail_verified") is True,"closed tranche END ACK missing")
req(p.get("interruption_recovery",{}).get("reconcile_first") is True,"interruption reconciliation missing")
req(p.get("foreground_handoff",{}).get("required_order",[])[0]=="RECONCILE_OLD_END_IF_CLOSE_INTENT_EXISTS","foreground close-intent reconciliation order drift")
req("PDF/report/artifact" in p.get("interruption_recovery",{}).get("in_tranche_deliverables",""),"intermediate deliverable classification missing")
req(p.get("automation_failover",{}).get("guarantee")=="FAILOVER_ONLY_NO_TRANCHE_DURATION_GUARANTEE" and p.get("automation_failover",{}).get("no_age_based_close") is True,"watchdog must not become a work timer")
if a.get("status")!="CLOSED":
    req(bool(a.get("start_provider_ts")) and bool(a.get("start_local")),"active tranche start timestamps missing")
    req(a.get("timing_mode")=="MILESTONE_BASED_NO_FIXED_DURATION","active tranche timing mode drift")
req(p.get("email_style",{}).get("parity_rule")=="GMAIL_START_AND_END_MUST_MATCH_OR_EXCEED_THE_HUMAN_EXPLANATION_GIVEN_IN_CHAT_NOT_MACHINE_ONLY_STATUS_PROSE","human explanatory Gmail parity missing")
print("communication-protocol-v7: PASS")
