#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(".")
SRC = ROOT / "app/src/main/java"
MANIFEST = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
GRADLE = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
ACTIVE = (SRC / "com/terminator364/kinlink/core/ActiveRecoveryPolicy.kt").read_text(encoding="utf-8")
AUTO = (SRC / "com/terminator364/kinlink/core/AutopilotRecoveryController.kt").read_text(encoding="utf-8")
PROBE = (SRC / "com/terminator364/kinlink/core/WifiDoctorProbe.kt").read_text(encoding="utf-8")
SERVICE = (SRC / "com/terminator364/kinlink/core/KinlinkObserverService.kt").read_text(encoding="utf-8")
OBSERVER = (SRC / "com/terminator364/kinlink/core/NetworkObserver.kt").read_text(encoding="utf-8")
OPT = (SRC / "com/terminator364/kinlink/core/WifiOptimizer.kt").read_text(encoding="utf-8")
LEDGER = (SRC / "com/terminator364/kinlink/data/TelemetryLedger.kt").read_text(encoding="utf-8")

def require(ok: bool, message: str) -> None:
    if not ok:
        raise SystemExit(message)

require('versionCode = 9' in GRADLE, "candidate contract: versionCode 9 missing")
require('versionName = "0.7.1"' in GRADLE, "candidate contract: versionName 0.7.1 missing")
require(
    'transport != Transport.WIFI -> RecoveryBlockReason.NON_WIFI' in ACTIVE,
    "candidate contract: central non-Wi-Fi active-recovery block missing",
)
require(
    'caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)' in AUTO,
    "candidate contract: execution-time Wi-Fi recheck missing",
)
require(
    'ABORT_METERED_WIFI' in AUTO,
    "candidate contract: automatic metered-Wi-Fi probe block missing",
)
require(
    'network.openConnection(URL(endpoint.url))' in PROBE,
    "candidate contract: probe is not pinned to the captured Android Network",
)
require(
    'meteredWifi -> WifiOptimizationAction.METERED_WIFI_REFRESH_ONLY' in OPT,
    "candidate contract: manual metered-Wi-Fi zero-HTTP branch missing",
)
require(
    'null, null, truth.failureDomain.name' in LEDGER,
    "candidate contract: new telemetry rows no longer prove NULL legacy interface/gateway persistence",
)
require(
    'fun countExactAction(action: String)' in LEDGER
    and 'fun countSuccessfulExactAction(action: String)' in LEDGER
    and 'fun latestExactActionTimestamp(action: String)' in LEDGER,
    "candidate contract: exact qualification receipt lookup helpers missing",
)
require(
    'android.permission.CHANGE_NETWORK_STATE' not in MANIFEST
    and 'android.permission.CHANGE_WIFI_STATE' not in MANIFEST
    and 'android.net.VpnService' not in MANIFEST,
    "candidate contract: forbidden network ownership surface present in manifest",
)

production = "\n".join(
    p.read_text(encoding="utf-8")
    for p in SRC.rglob("*.kt")
)

require(
    re.search(r"count(?:Successful)?Actions\(\s*QualificationReceiptNames", production) is None
    and re.search(r"latestActionTimestamp\(\s*QualificationReceiptNames", production) is None,
    "candidate contract: version-scoped qualification receipt still uses prefix matching",
)
forbidden = [
    "bindProcessToNetwork",
    "setProcessDefaultNetwork",
    "reportNetworkConnectivity",
    "requestNetwork(",
    "setUnderlyingNetworks",
]
for token in forbidden:
    require(token not in production, f"candidate contract: forbidden API token present: {token}")

def const_int(name: str, text: str) -> int:
    m = re.search(rf"const val {name}\s*=\s*([0-9_]+)", text)
    require(m is not None, f"candidate contract: constant {name} missing")
    return int(m.group(1).replace("_", ""))

connect_ms = const_int("CONNECT_TIMEOUT_MS", PROBE)
read_ms = const_int("READ_TIMEOUT_MS", PROBE)
attempt_hard_ms = const_int("ATTEMPT_HARD_TIMEOUT_MS", PROBE)
max_endpoints = const_int("MAX_ENDPOINTS", PROBE)
deadline_ms = const_int("RECOVERY_DEADLINE_MS", AUTO)
socket_envelope = max_endpoints * (connect_ms + read_ms)
hard_envelope = max_endpoints * attempt_hard_ms
require(
    socket_envelope < deadline_ms,
    f"candidate contract: socket timeout envelope {socket_envelope}ms is not below recovery deadline {deadline_ms}ms",
)
require(
    hard_envelope < deadline_ms,
    f"candidate contract: outer hard probe envelope {hard_envelope}ms is not below recovery deadline {deadline_ms}ms",
)
require(
    "TimeoutException" in PROBE
    and "future.get(" in PROBE
    and "connectionRef.getAndSet(null)?.disconnect()" in PROBE,
    "candidate contract: outer probe timeout/disconnect enforcement missing",
)

tests = list((ROOT / "app/src/test").rglob("*Test.kt"))
require(len(tests) >= 64, f"candidate contract: regression suite unexpectedly shrank to {len(tests)} tests")

print("candidate-contract: PASS")
print("version: 0.7.1 / code 9")
print(f"probe_socket_envelope_ms: {socket_envelope}")
print(f"probe_hard_envelope_ms: {hard_envelope}")
print(f"recovery_deadline_ms: {deadline_ms}")
print(f"test_files: {len(tests)}")

require(
    re.search(
        r"if\s*\(fieldQualificationReceiptWritten\)\s*\{\s*currentFieldQualificationVerdict\s*=\s*FieldCandidateQualificationVerdict\.PASS\s*return",
        SERVICE,
        re.S,
    ) is None,
    "candidate contract: historical qualified receipt can still force a stale PASS verdict",
)

require(
    "DefaultNetworkCallbackAcceptancePolicy" in OBSERVER
    and "callbackMatchesActive = network != null && network == activeNow" in OBSERVER,
    "candidate contract: stale non-default NetworkCallback events can reach truth/qualification logic",
)
