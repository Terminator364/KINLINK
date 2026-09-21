#!/usr/bin/env python3
import json
from pathlib import Path

ROOT=Path(".")
def load(p): return json.loads((ROOT/p).read_text(encoding="utf-8"))
def req(ok,msg):
    if not ok: raise SystemExit("continuity-context: "+msg)

m=load(".project-memory/CONTINUITY_CONTEXT_MANIFEST.json")
t=load(".project-memory/NEW_CONVERSATION_TAKEOVER.json")
s=load("project_state.json")
sc=load(".project-memory/PRODUCT_COMPLETION_SCORECARD.json")
ledger=load(".project-memory/FULL_REQUIREMENTS_LEDGER.json")
c=load(".project-memory/COMMUNICATION_PROTOCOL.json")

req(m.get("schema")=="kinlink.continuity_context_manifest/1","manifest schema missing")
req(m["immutable_history_anchors"]["preconception_anchor"]=="c565b6f1164d327caaa17e1de47d1cf48ed3c885","preconception anchor drifted")
required=set()
for layer in m["mandatory_context_layers"].values():
    required.update(layer)
required.add(".project-memory/CONTINUITY_CONTEXT_MANIFEST.json")
for p in required:
    req((ROOT/p).exists(),f"required context file missing: {p}")
    req(p in t["mandatory_first_reads"] or p==".project-memory/CONTINUITY_CONTEXT_MANIFEST.json",
        f"takeover does not load required context: {p}")

items=ledger["macro_capabilities"]
req(len(items)==m["full_completion_contract"]["expected_macro_capabilities"],
    f"macro capability count drifted: {len(items)}")
ids=[x["id"] for x in items]
req(len(ids)==len(set(ids)),"duplicate macro capability ids")
points=ledger["stage_points"]
calc=round(sum(points[x["stage"]] for x in items)/len(items)*100,1)
reported=float(sc["result"]["overall_percent"])
req(calc==reported,f"score mismatch computed={calc} reported={reported}")
req(reported==float(m["full_completion_contract"]["current_percent"]),
    "manifest completion score stale")
req(float(s["full_spec"]["overall_maturity_percent"])==reported,
    "project_state full-spec score stale")
req(s["full_spec"]["macro_capabilities"]==len(items),
    "project_state macro count stale")
req("recent traceability" in t["full_spec_rule"].lower(),
    "takeover no longer fences recent-slice amnesia")
req(c.get("field_feedback_intake",{}).get("rule","").startswith("SCREENSHOTS_OR_FIELD_EVIDENCE"),
    "field screenshot auto-counter-audit continuity missing")
req("Gmail" in (ROOT/"NEXT_UNCOMMITTED_ACTION.md").read_text(encoding="utf-8"),
    "next action no longer carries communication gate")
print(f"continuity-context: PASS | A+B+C macros={len(items)} | maturity={reported:.1f}%")
