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
req(p.get("schema")=="kinlink.communication_protocol/4","communication protocol v4 missing")
req(sm.get("schema")=="kinlink.communication_state_machine/4","communication state machine v4 missing")
req(a.get("schema")=="kinlink.active_tranche/5","active tranche v5 missing")
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
print("communication-protocol-v4: PASS")
