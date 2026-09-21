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
PRE_CANDIDATE = {
    "MACHINE",
    "MIGRATION",
    "SIGNER_CONTINUITY",
}
VALID = {"PASS", "PENDING", "BLOCKED"}

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--ledger",
        default=".project-memory/RELEASE_READINESS_0_7_0_DEV.json",
    )
    parser.add_argument("--audit", action="store_true")
    parser.add_argument("--require-field-candidate", action="store_true")
    parser.add_argument("--require-promotion", action="store_true")
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

    field_candidate_ready = all(
        gates[name]["status"] == "PASS" for name in PRE_CANDIDATE
    )
    promotion_ready = all(
        gates[name]["status"] == "PASS" for name in REQUIRED
    )

    declared_candidate = bool(data.get("field_candidate_allowed"))
    declared_promotion = bool(data.get("promotion_allowed"))
    if declared_candidate != field_candidate_ready:
        raise SystemExit(
            "field_candidate_allowed="
            f"{declared_candidate} inconsistent with pre_candidate_ready={field_candidate_ready}"
        )
    if declared_promotion != promotion_ready:
        raise SystemExit(
            f"promotion_allowed={declared_promotion} inconsistent with promotion_ready={promotion_ready}"
        )
    if declared_promotion and not declared_candidate:
        raise SystemExit("canonical promotion cannot be allowed before field-candidate eligibility")

    print("release-readiness-ledger: CONSISTENT")
    print("field_candidate_allowed:", declared_candidate)
    print("promotion_allowed:", declared_promotion)
    for name in sorted(REQUIRED):
        print(f"{name}: {gates[name]['status']}")

    if args.require_field_candidate and not field_candidate_ready:
        return 2
    if (args.require_promotion or args.require_ready) and not promotion_ready:
        return 3
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
