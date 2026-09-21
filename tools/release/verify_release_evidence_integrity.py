#!/usr/bin/env python3
import json
import re
from pathlib import Path

HEX64 = re.compile(r"^[0-9a-f]{64}$")
EXPECTED = {
    "unsigned_apk_sha256": "3c08caec397d49c8ab623cacb8e3f749cdae4fb23289a8d53b4e14b8cf0fafb8",
    "signed_apk_sha256": "cea3468340a8f81dc38cc1ba09abb68e8f9ccc1634e223f275b0e92b083ef0bc",
    "signer_cert_sha256": "2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3",
}

def require_hex64(label: str, value: str) -> None:
    if not isinstance(value, str) or not HEX64.fullmatch(value.lower()):
        raise SystemExit(f"{label} is not a 64-hex SHA-256: {value!r}")

def main() -> int:
    state = json.loads(Path("project_state.json").read_text(encoding="utf-8"))
    ledger = json.loads(
        Path(".project-memory/RELEASE_READINESS_0_7_0_DEV.json").read_text(encoding="utf-8")
    )
    evidence = Path("docs/RELEASE_EVIDENCE_0_7_0_FIELD_CANDIDATE.md").read_text(
        encoding="utf-8"
    ).lower()

    state_candidate = state["final_field_candidate"]
    ledger_candidate = ledger["signed_field_candidate"]
    machine = ledger["machine_artifact"]

    values = {
        "state.unsigned_apk_sha256": state_candidate["unsigned_apk_sha256"],
        "state.signed_apk_sha256": state_candidate["signed_apk_sha256"],
        "state.signer_cert_sha256": state_candidate["signer_cert_sha256"],
        "ledger.machine.unsigned_apk_sha256": machine["unsigned_apk_sha256"],
        "ledger.signed.unsigned_apk_sha256": ledger_candidate["unsigned_apk_sha256"],
        "ledger.signed.signed_apk_sha256": ledger_candidate["signed_apk_sha256"],
        "ledger.signed.signer_cert_sha256": ledger_candidate["signer_cert_sha256"],
    }
    for label, value in values.items():
        require_hex64(label, value)

    key_suffixes = {
        "unsigned_apk_sha256": (".unsigned_apk_sha256",),
        "signed_apk_sha256": (".signed_apk_sha256",),
        "signer_cert_sha256": (".signer_cert_sha256",),
    }
    for key, expected in EXPECTED.items():
        candidates = [
            value.lower()
            for label, value in values.items()
            if label.endswith(key_suffixes[key])
        ]
        if candidates and any(value != expected for value in candidates):
            raise SystemExit(f"{key} mismatch across durable evidence: {candidates}")
        if expected not in evidence:
            raise SystemExit(f"{key} missing from release evidence markdown")

    if state_candidate["source_commit"] != ledger_candidate["source_commit"]:
        raise SystemExit("source commit mismatch between project_state and release ledger")
    if state_candidate["ci_run"] != ledger_candidate["ci_run"]:
        raise SystemExit("CI run mismatch between project_state and release ledger")

    print("release-evidence-integrity: PASS")
    print("source_commit:", state_candidate["source_commit"])
    print("ci_run:", state_candidate["ci_run"])
    for key, value in EXPECTED.items():
        print(f"{key}: {value}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
