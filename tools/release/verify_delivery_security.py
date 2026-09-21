#!/usr/bin/env python3
import json
from pathlib import Path
def req(c,m):
    if not c: raise SystemExit(m)
p=json.loads(Path(".project-memory/DELIVERY_SECURITY_POLICY.json").read_text())
req(p.get("schema")=="kinlink.delivery_security_policy/1","delivery security schema missing")
i=p["installed_target"]; c=p["authorized_candidate"]; h=p["human_install_surface"]; r=p["rollback_surface"]
req(c["package"]=="com.terminator364.kinlink","package mismatch")
req(int(c["versionCode"])>int(i["versionCode"]),"candidate versionCode must be > installed")
req(c["signed_apk_sha256"]==c["drive_readback_sha256"] and c["drive_readback_status"]=="PASS","Drive readback mismatch")
req(h["filename"]=="KINLINK_INSTALL_NOW.apk" and h["apk_cardinality"]==1,"human surface ambiguous")
req(h["folder_id"]!=r["folder_id"],"rollback/install folders must differ")
req(p["platform_security"]["bypass_forbidden"] is True,"security bypass fence missing")
print("delivery-security-v1: PASS")
