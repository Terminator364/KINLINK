# KINLINK 0.7.3 target-device visual counter-audit

Verdict: **REJECT as near-final.** Installation succeeded, but the screenshots expose semantic and visual defects that machine responsive tests did not catch.

## Blocking findings

1. **False precision in quality:** Wi-Fi shows 100/100 and mobile 97/100 while the user reports poor real experience. Android validation/bandwidth estimates cannot justify those user-facing scores.
2. **Overclaiming label:** “Confortable” is derived from framework estimates and can contradict actual latency/throughput/stability.
3. **Improvement ambiguity:** the same UI says “aucun mieux confirmé ×3” while showing positive before/now deltas. That can be read as KINLINK caused improvement.
4. **Mode wrapping:** “Conservateur” breaks awkwardly on the real target phone.
5. **Data dialog:** too much explanatory prose in an interruptive modal; text/actions become visually oversized.
6. **Units:** MiB is technically defensible but not the requested human interface. Use adaptive decimal MB/GB.
7. **Jargon leakage:** WIFI_HEALTHY, MAXIMUM_STABILITY, BALANCE_UNKNOWN, HOLD_STEADY, NONE, etc. are implementation vocabulary, not primary user language.
8. **Hierarchy/duplication:** control-loop card repeats resource protection and occupies too much space relative to the actual actionable information.

## Research-backed correction

Android documentation states that NET_CAPABILITY_VALIDATED means public Internet access was validated, but a validated network can still lose connectivity or have poor signal. Android also describes NetworkCapabilities bandwidth values as estimates and recommends apps adapt based on what they actually observe. Therefore KINLINK must not translate these fields into “100/100 quality”.

TrafficStats reports device-wide mobile bytes since boot; it is not operator balance and not KINLINK-only traffic. UI must say so briefly and display human MB/GB.

Android accessibility guidance recommends at least 48dp touch targets; those remain mandatory. Manual screenshot review remains required because accessibility/static checks cannot judge semantic clarity.

## Successor rule

0.7.3 stays installed as the field baseline. The next user install is not 0.7.4. It is the coherent **0.8.0 integrated truth + control** line after fresh CI, rendered matrix, target-like visual audit, signing and Drive readback.
