#!/usr/bin/env python3
import json, subprocess
from pathlib import Path

ROOT=Path(".")
def load(p): return json.loads((ROOT/p).read_text(encoding="utf-8"))
def req(ok,msg):
    if not ok: raise SystemExit("resume-capsule: "+msg)

cap=load(".project-memory/RESUME_CAPSULE.json")
req(cap.get("schema")=="kinlink.resume_capsule/1","capsule schema")
req(cap["immutable_history"]["preconception_anchor"]=="c565b6f1164d327caaa17e1de47d1cf48ed3c885","preconception anchor drift")

for entry in cap["stable_blob_set"]:
    p=entry["path"]
    req((ROOT/p).exists(),f"missing stable input {p}")
    actual=subprocess.check_output(["git","hash-object",p],text=True).strip()
    req(actual==entry["git_blob_sha1"],f"stable input changed without capsule refresh: {p} expected={entry['git_blob_sha1']} actual={actual}")

score=load(".project-memory/PRODUCT_COMPLETION_SCORECARD.json")
b=load(".project-memory/B_EXPANSION_LEDGER.json")
program=load(".project-memory/INTEGRATED_PROGRAM_PLAN.json")
watch=load(".project-memory/COMMUNICATION_WATCHDOG_POLICY.json")
takeover=load(".project-memory/NEW_CONVERSATION_TAKEOVER.json")
truth=cap["canonical_truth"]

req(score["result"]["macro_capabilities"]==truth["macro_capabilities"],"macro count mismatch")
req(float(score["result"]["overall_percent"])==float(truth["maturity_percent"]),"maturity mismatch")
ids=[x["id"] for x in b["items"]]
req(len(ids)==truth["b_depth_count"],"B depth count mismatch")
first,last=truth["b_depth_range"].split("-",1)
req(ids[0]==first and ids[-1]==last,"B range mismatch")
req(b.get("version")==truth["b_depth_version"],"B version mismatch")
req(program.get("doctrine")=="ONE_COHERENT_FIELD_DELIVERY_AFTER_LARGE_INTERNAL_WAVES","integrated-program doctrine drift")
req(program.get("macro_denominator")==truth["macro_capabilities"],"program denominator mismatch")
req(program.get("current_maturity_percent")==truth["maturity_percent"],"program maturity mismatch")
req(watch.get("cardinality")==truth["communication_watchdog_cardinality"],"watchdog cardinality mismatch")
req(truth.get("no_micro_beta") is True,"no-micro-beta invariant missing")

for p in cap["fresh_state_files"]:
    req((ROOT/p).exists(),f"fresh state file missing: {p}")
    req(p in takeover["mandatory_first_reads"],f"takeover does not fresh-read {p}")

req(takeover["mandatory_first_reads"][0]==".project-memory/RESUME_CAPSULE.json","resume capsule must be first takeover read")
req("VERIFY_RESUME_CAPSULE_BLOBS_BEFORE_USING_DERIVED_CONTEXT" in takeover["takeover_algorithm"],"takeover capsule verification step missing")
print(f"resume-capsule: PASS | blobs={len(cap['stable_blob_set'])} | macros={truth['macro_capabilities']} | B={truth['b_depth_range']} | maturity={truth['maturity_percent']}%")
