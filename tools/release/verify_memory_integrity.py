#!/usr/bin/env python3
import json
from pathlib import Path

ROOT=Path(".")
def load(p): return json.loads((ROOT/p).read_text(encoding="utf-8"))
def req(ok,msg):
    if not ok: raise SystemExit("memory-integrity: "+msg)

policy=load(".project-memory/MEMORY_POLICY_V2.json")
manifest=load(".project-memory/CONTINUITY_CONTEXT_MANIFEST.json")
takeover=load(".project-memory/NEW_CONVERSATION_TAKEOVER.json")
pack=load(".project-memory/CANONICAL_CONTEXT_PACK.json")
score=load(".project-memory/PRODUCT_COMPLETION_SCORECARD.json")
b=load(".project-memory/B_EXPANSION_LEDGER.json")

req(policy["schema"]=="kinlink.project_memory_policy/2","memory policy schema")
req(policy["doctrine"]=="MEMORY_IS_PROVENANCE_AND_RECOVERY_INFRASTRUCTURE_NOT_A_CHAT_SUMMARY","doctrine drift")
req(len(b["items"])==70,"B31-B100 depth count must be 70")
ids=[x["id"] for x in b["items"]]
req(ids[0]=="B31" and ids[-1]=="B100","B depth range drift")
req(len(ids)==len(set(ids)),"duplicate B depth ids")
req(pack["immutable_A"]["preconception_anchor"]=="c565b6f1164d327caaa17e1de47d1cf48ed3c885","context pack A anchor drift")
req(pack["completion"]["macro_count"]==score["result"]["macro_capabilities"],"context pack macro count stale")
full=load(".project-memory/FULL_REQUIREMENTS_LEDGER.json")
score_by={x["id"]:x for x in score["epics"]}
ledger_by={x["id"]:x for x in full["macro_capabilities"]}
req(set(score_by)==set(ledger_by),"scorecard/ledger macro id mismatch")
for mid in sorted(score_by):
    req(score_by[mid]["stage"]==ledger_by[mid]["stage"],f"scorecard/ledger stage mismatch: {mid}")
    req(float(score_by[mid]["points"])==float(ledger_by[mid]["points"]),f"scorecard/ledger points mismatch: {mid}")
req(float(pack["completion"]["maturity_percent"])==float(score["result"]["overall_percent"]),"context pack maturity stale")

chron=[]
seen_e=set()
seen_k=set()
for line in (ROOT/".project-memory/PROJECT_CHRONICLE.jsonl").read_text(encoding="utf-8").splitlines():
    if not line.strip(): continue
    e=json.loads(line)
    for key in ["event_id","source","type","time_wall","project_id","actor","authority_tier","idempotency_key","privacy_class","evidence_refs","status","summary"]:
        req(key in e,f"chronicle event missing {key}")
    req(e["event_id"] not in seen_e,f"duplicate event_id {e['event_id']}")
    req(e["idempotency_key"] not in seen_k,f"duplicate idempotency key {e['idempotency_key']}")
    seen_e.add(e["event_id"]); seen_k.add(e["idempotency_key"]); chron.append(e)
req(len(chron)>=4,"chronicle unexpectedly empty")

sup=load(".project-memory/SUPERSESSION_LEDGER.json")
for s in sup["entries"]:
    for key in ["supersession_id","old_ref","new_ref","reason","evidence_refs","effective_at"]:
        req(key in s,f"supersession missing {key}")

required_memory=[
    ".project-memory/RESUME_CAPSULE.json",
    ".project-memory/MEMORY_POLICY_V2.json",
    ".project-memory/COMMUNICATION_WATCHDOG_POLICY.json",
    ".project-memory/DEAD_END_REGISTRY.json",
    ".project-memory/PROJECT_CHRONICLE.jsonl",
    ".project-memory/CANONICAL_CONTEXT_PACK.json",
    ".project-memory/SUPERSESSION_LEDGER.json",
    ".project-memory/B_EXPANSION_LEDGER.json",
    "docs/B_EXPANSION_V2.md",
    "docs/B_EXPANSION_V3.md",
    "docs/B_EXPANSION_V4.md",
    "docs/B_EXPANSION_V5.md",
]
for p in required_memory:
    req((ROOT/p).exists(),f"missing {p}")
    req(p in takeover["mandatory_first_reads"],f"takeover does not read {p}")
all_layers={p for values in manifest["mandatory_context_layers"].values() for p in values}
for p in required_memory:
    req(p in all_layers or p=="docs/B_EXPANSION_V2.md",f"context manifest does not cover {p}")

req("CONFLICT" in policy["admission_states"],"conflict/HOLD model missing")
req("READBACK" in policy["write_protocol"],"readback missing")
req("APPEND_CHRONICLE_EVENT" in policy["write_protocol"],"chronicle append missing")
req("REFRESH_DERIVED_CONTEXT_PACK" in policy["write_protocol"],"context pack refresh missing")
watch=load(".project-memory/COMMUNICATION_WATCHDOG_POLICY.json")
req(watch.get("schema")=="kinlink.communication_watchdog_policy/2","watchdog policy schema")
req(watch.get("cardinality")==1,"watchdog cardinality must remain exactly one")
req(watch.get("name")=="KINLINK Continuity & Comms","watchdog name drift")
req(watch.get("cadence")=="HOURLY","watchdog cadence drift")
req(watch.get("stale_work_threshold_minutes")==25,"watchdog stale-work threshold drift")
req(watch.get("foreground_resume",{}).get("auto_open_new_start") is True,"watchdog user-authorized continuation drift")
dead=load(".project-memory/DEAD_END_REGISTRY.json")
req(dead.get("schema")=="kinlink.dead_end_registry/1","dead-end registry schema")
req(len(dead.get("entries",[]))>=7,"dead-end registry unexpectedly small")
dead_ids=[x["id"] for x in dead["entries"]]
req(len(dead_ids)==len(set(dead_ids)),"duplicate dead-end ids")
for e in dead["entries"]:
    for key in ["premise","verdict","evidence","safe_alternative","supersession_condition"]:
        req(key in e,f"dead-end entry missing {key}")
req(pack.get("communication",{}).get("watchdog_cardinality")==1,"context pack watchdog cardinality stale")
cap=load(".project-memory/RESUME_CAPSULE.json")
req(cap.get("schema")=="kinlink.resume_capsule/1","resume capsule schema")
req(cap["canonical_truth"]["b_depth_range"]=="B31-B100","resume capsule B depth stale")
req(cap["canonical_truth"]["macro_capabilities"]==score["result"]["macro_capabilities"],"resume capsule macro count stale")
print(f"memory-integrity: PASS | chronicle={len(chron)} | B-depth={len(ids)} | macros={score['result']['macro_capabilities']} | maturity={score['result']['overall_percent']}%")
