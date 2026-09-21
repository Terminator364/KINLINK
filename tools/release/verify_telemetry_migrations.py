#!/usr/bin/env python3
import json
import re
import sqlite3
import textwrap
from pathlib import Path

PLAN = Path("app/src/main/java/com/terminator364/kinlink/data/TelemetryMigrationPlan.kt")

V1_NETWORK_SCHEMA = """
CREATE TABLE network_events (
  event_id TEXT PRIMARY KEY,
  ts_wall_ms INTEGER NOT NULL,
  transport TEXT NOT NULL,
  internet_state TEXT NOT NULL,
  context_type TEXT NOT NULL,
  metered INTEGER NOT NULL,
  interface_name TEXT,
  gateway TEXT,
  failure_domain TEXT NOT NULL,
  confidence REAL NOT NULL
)
"""

REQUIRED_NETWORK_V5 = {
    "event_id", "ts_wall_ms", "transport", "internet_state", "context_type",
    "metered", "interface_name", "gateway", "failure_domain", "confidence",
    "quality_tier", "ipv4_address", "ipv6_address",
    "ipv4_default_route", "ipv6_default_route",
}
REQUIRED_ACTION_V5 = {
    "receipt_id", "ts_wall_ms", "action", "success", "summary", "duration_ms",
}


def decode_kotlin_string(token: str) -> str:
    token = token.strip()
    if token.startswith('"""'):
        end = token.rfind('"""')
        return textwrap.dedent(token[3:end]).strip()
    return json.loads(token)


def parse_steps(source: str):
    starts = [
        m.start()
        for m in re.finditer(r"TelemetryMigrationStep\s*\(\s*\d+\s*,", source)
    ]
    steps = []
    for i, start in enumerate(starts):
        end = starts[i + 1] if i + 1 < len(starts) else source.find("\n    )\n", start)
        if end < 0:
            end = len(source)
        chunk = source[start:end]
        header = re.search(r"TelemetryMigrationStep\s*\(\s*(\d+)\s*,\s*(\d+)\s*,", chunk)
        if not header:
            raise RuntimeError(f"cannot parse migration header near offset {start}")
        from_v, to_v = map(int, header.groups())
        body = chunk[header.end():]
        tokens = re.findall(r'"""[\s\S]*?"""(?:\.trimIndent\(\))?|"(?:\\.|[^"\\])*"', body)
        sql = []
        for token in tokens:
            value = decode_kotlin_string(token)
            if re.match(r"^(CREATE|ALTER)\b", value.strip(), re.I):
                sql.append(value)
        if not sql:
            raise RuntimeError(f"no SQL parsed for migration {from_v}->{to_v}")
        steps.append((from_v, to_v, sql))
    return steps


def columns(conn, table):
    return {row[1] for row in conn.execute(f"PRAGMA table_info({table})")}


def apply(conn, steps, old, new):
    expected = old
    for from_v, to_v, sqls in steps:
        if from_v < old or to_v > new:
            continue
        if from_v != expected or to_v != expected + 1:
            raise AssertionError(f"non-contiguous path at {from_v}->{to_v}, expected {expected}->{expected+1}")
        for sql in sqls:
            conn.execute(sql)
        expected = to_v
    if expected != new:
        raise AssertionError(f"path ended at {expected}, expected {new}")


def build_version(steps, version):
    conn = sqlite3.connect(":memory:")
    conn.execute(V1_NETWORK_SCHEMA)
    conn.execute(
        "INSERT INTO network_events(event_id,ts_wall_ms,transport,internet_state,context_type,metered,interface_name,gateway,failure_domain,confidence) "
        "VALUES('sentinel-network',1,'WIFI','VALIDATED','DEFAULT',0,NULL,NULL,'NONE',1.0)"
    )
    if version > 1:
        apply(conn, steps, 1, version)
    if version >= 2:
        action_cols = columns(conn, "action_receipts")
        names = ["receipt_id", "ts_wall_ms", "action", "success", "summary"]
        vals = ["sentinel-action", 1, "TEST", 1, "preserve-me"]
        if "duration_ms" in action_cols:
            names.append("duration_ms")
            vals.append(42)
        placeholders = ",".join("?" for _ in vals)
        conn.execute(
            f"INSERT INTO action_receipts({','.join(names)}) VALUES({placeholders})",
            vals,
        )
    conn.commit()
    return conn


def main():
    source = PLAN.read_text(encoding="utf-8")
    steps = parse_steps(source)
    assert [(a, b) for a, b, _ in steps] == [(1,2),(2,3),(3,4),(4,5)], steps

    for old in range(1, 5):
        conn = build_version(steps, old)
        apply(conn, steps, old, 5)
        assert columns(conn, "network_events") >= REQUIRED_NETWORK_V5
        assert columns(conn, "action_receipts") >= REQUIRED_ACTION_V5
        row = conn.execute(
            "SELECT event_id, transport, internet_state FROM network_events WHERE event_id='sentinel-network'"
        ).fetchone()
        assert row == ("sentinel-network", "WIFI", "VALIDATED"), (old, row)
        if old >= 2:
            action = conn.execute(
                "SELECT receipt_id, action, summary FROM action_receipts WHERE receipt_id='sentinel-action'"
            ).fetchone()
            assert action == ("sentinel-action", "TEST", "preserve-me"), (old, action)
        conn.close()
        print(f"migration v{old}->v5 executable + data-preserving: PASS")

    print("telemetry-migration-runtime-simulation: PASS")


if __name__ == "__main__":
    main()
