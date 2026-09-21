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
req(p.get("schema")=="kinlink.communication_protocol/5","communication protocol v4 missing")
req(sm.get("schema")=="kinlink.communication_state_machine/5","communication state machine v4 missing")
req(a.get("schema")=="kinlink.active_tranche/6","active tranche v5 missing")
req(ds.get("schema")=="kinlink.delivery_security_policy/1","delivery security policy missing")
req(p.get("cadence_minutes")==25 and p.get("primary_work_budget_minutes")==22 and p.get("closeout_reserve_minutes")==3,"cadence broken")
req(p.get("final_reply_gate",{}).get("required_delivery_state")=="END_ACKNOWLEDGED_OR_CLOSED","final reply gate missing")
req(p.get("human_install_surface",{}).get("max_apk_cardinality")==1,"single installer gate missing")
req(p.get("security_continuity",{}).get("platform_security_checks")=="OBEY_AND_NEVER_BYPASS","security continuity missing")
req(bool(a.get("delivery_key")),"delivery key missing")
req(bool(a.get("gmail_start_message_id")) and bool(a.get("gmail_start_thread_id")),"START ACK missing")
req(any(r.get("delivery_key")==a.get("delivery_key") and r.get("event")=="START_ACKNOWLEDGED" for r in rows),"ledger START missing")
if a.get("status")=="CLOSED":
    req(bool(a.get("gmail_end_message_id")) and a.get("end_mail_verified") is True,"closed tranche END ACK missing")
req(p.get("interruption_recovery",{}).get("reconcile_first") is True,"interruption reconciliation missing")
req(p.get("close_intent_must_complete",{}).get("missed_end_class")=="P0_COMMUNICATION_FAULT","missed-END P0 class missing")
req(p.get("foreground_handoff",{}).get("required_order",[])[:2]==["RECONCILE_OLD_END","PERSIST_CLOSED"],"foreground missed-END reconciliation order drift")
req("PDF/report/artifact" in p.get("interruption_recovery",{}).get("in_tranche_deliverables",""),"intermediate deliverable classification missing")
print("communication-protocol-v5: PASS")
