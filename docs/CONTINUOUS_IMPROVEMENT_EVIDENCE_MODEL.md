# KINLINK continuous improvement evidence model

## User requirement

KINLINK must behave like continuous care:
- observe;
- detect deterioration;
- act only when justified;
- verify what happened after the action;
- keep watching;
- if quality later falls again, re-evaluate;
- do this without heat, battery drain, RAM spikes or repeated useless work.

## 0.7.2 design

### Zero-wakeup follow-up

`MobileAssistEvidenceTracker` uses only normal Android network callbacks.
It adds no repeating timer, WorkManager, AlarmManager or polling loop.

### Evidence states

- `METRICS_AVAILABLE`
  - Android started exposing capacity after a refresh.
  - This proves observability improvement only.

- `SUSTAINED_BETTER`
  - passive quality is better than baseline;
  - it remains better for at least 20 seconds;
  - wording is explicitly correlational, not causal.

- `RELAPSED`
  - quality briefly improved but fell back before the 20-second confirmation.

- `RELAPSED_AFTER_SUSTAINED`
  - a sustained improvement was confirmed;
  - quality later fell back to the original baseline within a 10-minute passive watch window;
  - this makes later degradation visible and allows a new bounded re-evaluation after cooldown.

- `NO_BETTER`
  - no better passive quality was observed after 60 seconds.

- `INCONCLUSIVE`
  - transport/validation changed, clock was inconsistent, or the evidence window expired.

### Bounded timing

- sustain confirmation: 20 s;
- no-benefit decision: 60 s;
- evidence max window: 180 s;
- post-sustained relapse watch: 10 min;
- automatic Mobile Assist cooldown: 5 min;
- automatic action cap: 6/hour.

### Anti-repeat

Two recent `NO_BETTER`/early `RELAPSED` results pause repeated automatic assistance.

`RELAPSED_AFTER_SUSTAINED` is **not** treated as proof the earlier action was useless. A connection can legitimately improve and later deteriorate again. After normal cooldown/resource gates, KINLINK may re-evaluate.

## Resource discipline

Healthy validated mobile callbacks now return before:
- SQLite action-history queries;
- resource sampling;
- action arbitration.

This keeps the common healthy path cheap.

Active work remains suppressed when:
- safe mode is ON;
- mobile budget is LOW / EXHAUSTED / EXPIRED;
- Android says network suspended;
- passive mobile radio is weak enough that a metric refresh cannot fix coverage;
- RAM/battery/thermal guard says constrained;
- cooldown/hourly/anti-repeat fences apply.

## Causal-proof boundary

The installed 0.7.1 / current Level-1 mechanism does **not** have a causal throughput accelerator.

`requestBandwidthUpdate()` asks Android to refresh bandwidth information.
It does not itself bond links, change tower selection, rewrite routing, or increase radio capacity.

Therefore 0.7.2 separates:
- **continuous observation and evidence** — current line;
- **strong stabilizer/data plane** — separately gated future work.

A strong stabilizer may only claim improvement if a qualified mechanism actually changes traffic handling and A/B-style evidence demonstrates benefit without violating RAM/battery/latency/fail-open constraints.
