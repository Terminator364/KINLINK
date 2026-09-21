# KINLINK — cold takeover simulation

This verifier models the first minutes of a brand-new conversation without using chat history.

It proves that:
- the resume capsule is first;
- project_state agrees with the full A+B+C score and B depth;
- communication protocol version is current;
- installed and active-successor versions are not confused;
- BUILD_STATUS and NEXT_UNCOMMITTED_ACTION name the same latest engineering head as project_state;
- the current tranche has a valid START/CLOSE/END transition shape in the durable ledger.

CI command:

```
python tools/release/verify_resume_capsule.py
python tools/release/verify_cold_takeover.py
```

A failure is a continuity HOLD. A new conversation must reconcile the durable state rather than guess.
