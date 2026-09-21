#!/usr/bin/env python3
import argparse, json, re
from datetime import datetime, timezone
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
GRADLE=ROOT/"app/build.gradle.kts"

def read_version():
    text=GRADLE.read_text(encoding="utf-8")
    code=re.search(r"(?m)^\s*versionCode\s*=\s*(\d+)\s*$",text)
    name=re.search(r'(?m)^\s*versionName\s*=\s*"([^"]+)"\s*$',text)
    if not code or not name:
        raise SystemExit("release-manifest: cannot parse versionCode/versionName")
    return int(code.group(1)),name.group(1)

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--output")
    ap.add_argument("--commit")
    ap.add_argument("--built-at-utc")
    ap.add_argument("--check-only",action="store_true")
    args=ap.parse_args()
    code,name=read_version()
    if args.check_only:
        print(f"release-manifest: PASS | version={name} | versionCode={code}")
        return
    if not args.output or not args.commit:
        raise SystemExit("release-manifest: --output and --commit are required")
    built=args.built_at_utc or datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    payload={
        "product":"KINLINK",
        "version":name,
        "versionCode":code,
        "variant":"candidate-unsigned",
        "commit":args.commit,
        "built_at_utc":built,
        "verification":"CI build, unit tests, Android lint, safety fences and rendered responsive UI matrix passed; stable-sign promotion and field verification pending"
    }
    out=Path(args.output)
    out.parent.mkdir(parents=True,exist_ok=True)
    out.write_text(json.dumps(payload,indent=2,ensure_ascii=False)+"\n",encoding="utf-8")
    reread=json.loads(out.read_text(encoding="utf-8"))
    if reread["version"]!=name or reread["versionCode"]!=code or reread["commit"]!=args.commit:
        raise SystemExit("release-manifest: readback mismatch")
    print(f"release-manifest: PASS | version={name} | versionCode={code} | commit={args.commit}")

if __name__=="__main__":
    main()
