#!/usr/bin/env python3
import json
from pathlib import Path

ROOT=Path(".")
def load(p): return json.loads((ROOT/p).read_text(encoding="utf-8"))
def req(ok,msg):
    if not ok: raise SystemExit("cold-takeover: "+msg)

cap=load(".project-memory/RESUME_CAPSULE.json")
state=load("project_state.json")
active=load(".project-memory/ACTIVE_TRANCHE.json")
takeover=load(".project-memory/NEW_CONVERSATION_TAKEOVER.json")
ledger=[]
for line in (ROOT/".project-memory/COMMUNICATION_DELIVERY_LEDGER.jsonl").read_text(encoding="utf-8").splitlines():
    if line.strip():
        ledger.append(json.loads(line))

truth=cap["canonical_truth"]
req(state["full_spec"]["macro_capabilities"]==truth["macro_capabilities"],"project_state macro count stale")
req(float(state["full_spec"]["overall_maturity_percent"])==float(truth["maturity_percent"]),"project_state maturity stale")
req(state["full_spec"]["b_depth_range"]==truth["b_depth_range"],"project_state B depth stale")
req(state["communication_protocol"]["version"]==6,"project_state communication protocol stale")
req(state["continuity"]["resume_capsule"]==".project-memory/RESUME_CAPSULE.json","project_state resume capsule missing")
req(state["continuity"]["cold_takeover_verifier"]=="tools/release/verify_cold_takeover.py","project_state cold takeover verifier missing")
req(state["active_integrated_successor"]["version"]=="0.8.0-dev","active successor version stale")
req(state["active_integrated_successor"]["versionCode"]==12,"active successor versionCode stale")
req(state.get("state_authority",{}).get("schema")=="kinlink.state_authority/1","project_state authority map missing")
req(state["full_spec"]["active_wave"]==state["active_integrated_successor"]["active_wave"],"active wave drift between full_spec and successor")
req(state["full_spec"]["active_wave"]=="W2_CONTEXT_AND_MOBILE_VAULT_CORE","cold takeover did not advance to W2")

next_text=(ROOT/"NEXT_UNCOMMITTED_ACTION.md").read_text(encoding="utf-8")
build_text=(ROOT/"BUILD_STATUS.md").read_text(encoding="utf-8")
for token in [truth["b_depth_range"],"RESUME_CAPSULE","Gmail START","Gmail END"]:
    req(token in next_text,f"NEXT_UNCOMMITTED_ACTION missing {token}")
maturity=float(truth["maturity_percent"])
maturity_tokens={f"{maturity:g}%",f"{maturity:.1f}%"}
req(any(t in next_text for t in maturity_tokens),f"NEXT_UNCOMMITTED_ACTION maturity token missing: {sorted(maturity_tokens)}")
latest=state["active_integrated_successor"]["latest_head"]
req(latest in next_text,"NEXT_UNCOMMITTED_ACTION does not name latest engineering head")
req("0.7.3-dev / versionCode 11" in build_text,"BUILD_STATUS installed baseline stale")
req(latest in build_text,"BUILD_STATUS latest head stale")

key=active.get("delivery_key")
events=[e for e in ledger if e.get("delivery_key")==key]
starts=[e for e in events if e.get("event")=="START_ACKNOWLEDGED"]
ends=[e for e in events if e.get("event")=="END_ACKNOWLEDGED"]
closes=[e for e in events if e.get("event") in ("CLOSE_INTENT","INTERRUPTION_RECOVERY_CLOSE_INTENT")]
req(len(starts)==1,f"active tranche must have exactly one START_ACK: {key}")
status=active.get("status")
delivery=active.get("delivery_state")
if status=="ACTIVE":
    req(delivery=="START_ACKNOWLEDGED","ACTIVE tranche delivery state mismatch")
    req(len(ends)==0,"ACTIVE tranche already has END_ACK")
elif status=="CLOSING":
    req(active.get("close_intent_persisted") is True,"CLOSING without close intent")
    req(len(closes)>=1,"CLOSING without ledger close intent")
    req(len(ends)==0,"CLOSING already has END_ACK")
elif status=="CLOSED":
    req(delivery=="CLOSED","CLOSED tranche delivery state mismatch")
    req(len(ends)>=1,"CLOSED tranche missing END_ACK")
    req(active.get("gmail_end_message_id")==ends[-1].get("gmail_end_message_id"),"CLOSED Gmail END id mismatch")
else:
    raise SystemExit(f"cold-takeover: unknown active-tranche status {status}")

req(takeover["mandatory_first_reads"][0]==".project-memory/RESUME_CAPSULE.json","takeover does not start from resume capsule")
print(f"cold-takeover: PASS | tranche={active['tranche_id']} | status={status} | latest={latest[:12]} | macros={truth['macro_capabilities']} | B={truth['b_depth_range']}")
