#!/usr/bin/env python3
import argparse
import json
from pathlib import Path

REQUIRED = {
    "MACHINE",
    "MIGRATION",
    "SIGNER_CONTINUITY",
    "FIELD_HANDOFF",
    "RESOURCE_QUALIFICATION",
}
VALID = {"PASS", "PENDING", "BLOCKED"}

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--ledger",
        default=".project-memory/RELEASE_READINESS_0_7_0_DEV.json",
    )
    parser.add_argument("--audit", action="store_true")
    parser.add_argument("--require-ready", action="store_true")
    args = parser.parse_args()

    data = json.loads(Path(args.ledger).read_text(encoding="utf-8"))
    gates = data.get("gates", {})
    missing = REQUIRED - set(gates)
    extra = set(gates) - REQUIRED
    if missing or extra:
        raise SystemExit(f"gate set mismatch: missing={sorted(missing)} extra={sorted(extra)}")

    invalid = {
        name: gate.get("status")
        for name, gate in gates.items()
        if gate.get("status") not in VALID
    }
    if invalid:
        raise SystemExit(f"invalid gate statuses: {invalid}")

    all_pass = all(gates[name]["status"] == "PASS" for name in REQUIRED)
    declared = bool(data.get("promotion_allowed"))
    if declared != all_pass:
        raise SystemExit(
            f"promotion_allowed={declared} inconsistent with all_pass={all_pass}"
        )

    print("release-readiness-ledger: CONSISTENT")
    print("promotion_allowed:", declared)
    for name in sorted(REQUIRED):
        print(f"{name}: {gates[name]['status']}")

    if args.require_ready and not all_pass:
        return 2
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
