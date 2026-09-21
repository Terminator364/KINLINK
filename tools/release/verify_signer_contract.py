#!/usr/bin/env python3
import json
import re
from pathlib import Path

CANONICAL_SIGNER_SHA256 = "2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3"

def main() -> int:
    if not re.fullmatch(r"[0-9a-f]{64}", CANONICAL_SIGNER_SHA256):
        raise SystemExit("canonical signer fingerprint is malformed")

    state = json.loads(Path("project_state.json").read_text(encoding="utf-8"))
    state_fp = (
        state.get("release_candidate", {})
        .get("signer_cert_sha256", "")
        .lower()
    )
    if state_fp != CANONICAL_SIGNER_SHA256:
        raise SystemExit(
            f"project_state signer mismatch: {state_fp or '<missing>'}"
        )

    receipt = Path("docs/PROMOTION_RECEIPT_0_6_0_RC3.md").read_text(encoding="utf-8").lower()
    pipeline = Path("docs/STABLE_SIGNING_PIPELINE.md").read_text(encoding="utf-8").lower()

    if CANONICAL_SIGNER_SHA256 not in receipt:
        raise SystemExit("RC3 promotion receipt does not carry the canonical signer fingerprint")
    if CANONICAL_SIGNER_SHA256 not in pipeline:
        raise SystemExit("stable signing pipeline does not pin the canonical signer fingerprint")

    print("signer-contract: PASS")
    print("canonical signer SHA-256:", CANONICAL_SIGNER_SHA256)
    print("private signing material remains outside public CI")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
