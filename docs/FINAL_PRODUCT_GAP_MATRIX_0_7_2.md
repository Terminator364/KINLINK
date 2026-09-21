# KINLINK final-product gap matrix — 0.7.2 dev

## Goal

Drive KINLINK toward the closest defensible version of the final product without hiding unproven behavior behind labels such as “optimized”.

| Area | Current state | Evidence | Gap before final-like status |
|---|---|---|---|
| Wi-Fi fail-open recovery | HARDENED | bounded probes, handoff abort, watchdog, circuit breaker | field history on unstable home Wi-Fi |
| Cellular awareness | HARDENED L1 | passive quality/cause, budget/resource gates | stronger causal intervention not yet qualified |
| Cellular “improvement” proof | HARDENED HONESTLY | sustained/passive score evidence + relapse | no claim of throughput causality from metric refresh |
| Continuous care | 0.7.2 DEV | event-driven re-evaluation + bounded three-shot confirmation + 10 min relapse watch | field proof on target phone |
| UI | 0.7.2 DEV | compact status/action/proof cockpit, contextual primary action | target-phone screenshot validation |
| Battery / RAM | HARDENED + FIELD GATE | runtime PSS/battery gate, healthy fast path | versionCode 10 field resource qualification |
| Thermal | HARDENED 0.7.2 | active work pauses from Android moderate thermal state | target-phone thermal field evidence |
| Data-cost protection | HARDENED | no automatic cellular HTTP/DNS/speedtest; budget suppression | user-selected data threshold remains optional |
| Privacy | HARDENED | bounded local telemetry, no raw gateway/interface in new history | field export review |
| Strong stabilizer | GATED M5 | architecture only | prototype + A/B benefit + fail-open + resource qualification |
| Release integrity | HARDENED | exact-head CI, stable signing, Drive readback, frozen candidate | repeat for eventual 0.7.2 candidate |

## Definition of “continuous improvement”

KINLINK may call its behavior continuous only if all of these are true:

1. degradation is detected without a polling storm;
2. the chosen action is justified for the current cause;
3. the action is bounded by cooldown/hour/resource/data gates;
4. the post-action state is measured;
5. a transient improvement is not counted as durable;
6. a later relapse is detected;
7. the next response happens only after normal safety gates;
8. repeated non-benefit causes suppression;
9. the user can see proof/history;
10. battery/RAM/thermal protection outranks aggressive retry.

0.7.2 implements this control model for Level-1 Mobile Assist. It still does not claim that Android bandwidth-metric refresh increases carrier throughput.

## Causal-proof requirement

A future strong stabilizer may claim network improvement only when:
- the mechanism actually changes traffic handling;
- pre/post or controlled comparison evidence is available;
- improvement persists beyond a confirmation window;
- added latency, RAM, battery and thermal cost remain inside qualification limits;
- teardown restores Android-native networking immediately.

Until then, the product wording must say “measurement refreshed”, “correlated sustained improvement” or “no improvement observed”, never “KINLINK made 4G faster”.
