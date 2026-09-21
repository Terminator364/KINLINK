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
MOBILE_ASSIST = (SRC / "com/terminator364/kinlink/core/MobileAssistController.kt").read_text(encoding="utf-8")
MOBILE_POLICY = (SRC / "com/terminator364/kinlink/core/MobileAssistPolicy.kt").read_text(encoding="utf-8")
LEDGER = (SRC / "com/terminator364/kinlink/data/TelemetryLedger.kt").read_text(encoding="utf-8")

def require(ok: bool, message: str) -> None:
    if not ok:
        raise SystemExit(message)

require('versionCode = 12' in GRADLE, "candidate contract: versionCode 12 missing")
require('versionName = "0.8.0-dev"' in GRADLE, "candidate contract: versionName 0.8.0-dev missing")
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
require(
    "Executors.newSingleThreadExecutor" not in PROBE,
    "candidate contract: HTTP probe reintroduced per-attempt unbounded helper thread allocation",
)
BOUNDED_EXECUTOR = (SRC / "com/terminator364/kinlink/core/BoundedProbeExecutor.kt").read_text(encoding="utf-8")
require(
    "MAX_CONCURRENT_ATTEMPTS = 2" in BOUNDED_EXECUTOR
    and "SynchronousQueue" in BOUNDED_EXECUTOR
    and "AbortPolicy" in BOUNDED_EXECUTOR,
    "candidate contract: bounded shared diagnostic executor missing",
)

tests = list((ROOT / "app/src/test").rglob("*Test.kt"))
require(len(tests) >= 64, f"candidate contract: regression suite unexpectedly shrank to {len(tests)} tests")

print("candidate-contract: PASS")
print("version: 0.8.0-dev / code 12")
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
    and "callbackMatchesActive = network != null && network == activeNow" in OBSERVER
    and "acceptStable(" in OBSERVER
    and "callbackMatchedAfterReduction = network != null && network == cm.activeNetwork" in OBSERVER,
    "candidate contract: NetworkCallback active-default identity is not fenced before and after reduction",
)
require(
    "shouldCancelPendingLoss(" in OBSERVER
    and "network == cm.activeNetwork" in OBSERVER,
    "candidate contract: stale callback can cancel pending loss-settle generation",
)

require(
    "WifiOptimizationContinuationPolicy.mayRefresh(" in OPT
    and "sameActiveNetwork = activeAfterDiagnostics == network" in OPT
    and "WifiOptimizationAction.HANDOFF_ABORTED" in OPT,
    "candidate contract: manual Wi-Fi optimization can continue after active-network handoff",
)
require(
    "requestBandwidthUpdate(network)" in MOBILE_ASSIST
    and "NetworkCapabilities.TRANSPORT_CELLULAR" in MOBILE_ASSIST,
    "candidate contract: Mobile Assist cellular metric refresh path missing",
)
for token in [
    "openConnection(",
    "getAllByName(",
    "Socket(",
    "HttpURLConnection",
    "requestNetwork(",
    "bindProcessToNetwork",
    "reportNetworkConnectivity",
]:
    require(
        token not in MOBILE_ASSIST,
        f"candidate contract: Mobile Assist may emit hidden network traffic or seize routing: {token}",
    )
require(
    "COOLDOWN_MS = 5L * 60L * 1000L" in MOBILE_POLICY
    and "MAX_ACTIONS_PER_HOUR = 6" in MOBILE_POLICY,
    "candidate contract: Mobile Assist cooldown/hourly cap missing",
)
require(
    "BudgetState.BUNDLE_LOW" in MOBILE_POLICY
    and "BudgetState.BUNDLE_EXHAUSTED" in MOBILE_POLICY
    and "BudgetState.BUNDLE_EXPIRED" in MOBILE_POLICY,
    "candidate contract: Mobile Assist does not protect paid-data budget states",
)
require(
    "NETWORK_SUSPENDED" in MOBILE_POLICY
    and "resourceConstrained" in MOBILE_POLICY
    and "OBSERVATION_ONLY" in MOBILE_POLICY,
    "candidate contract: Mobile Assist fail-open/resource/safe-mode gates missing",
)
require(
    "MobileRadioQuality.WEAK" in MOBILE_POLICY
    and "MobileAssistBlockReason.WEAK_SIGNAL" in MOBILE_POLICY,
    "candidate contract: weak cellular radio no-op fence missing",
)

EVIDENCE_TRACKER = (SRC / "com/terminator364/kinlink/core/MobileAssistEvidenceTracker.kt").read_text(encoding="utf-8")
require(
    "MIN_SUSTAINED_BETTER_MS = 20_000L" in EVIDENCE_TRACKER
    and "NO_BENEFIT_AFTER_MS = 60_000L" in EVIDENCE_TRACKER
    and "RELAPSE_MONITOR_WINDOW_MS = 10L * 60L * 1000L" in EVIDENCE_TRACKER,
    "candidate contract: sustained Mobile Assist evidence windows missing",
)
require(
    "RELAPSED_AFTER_SUSTAINED" in EVIDENCE_TRACKER
    and "causalité non affirmée" in EVIDENCE_TRACKER,
    "candidate contract: Mobile Assist relapse/truthfulness evidence missing",
)
require(
    "MOBILE_ASSIST_EVIDENCE_" in SERVICE,
    "candidate contract: service does not persist Mobile Assist evidence",
)

QUALITY_SCORE = (SRC / "com/terminator364/kinlink/core/PassiveQualityScorePolicy.kt").read_text(encoding="utf-8")
COCKPIT_ACTION = (SRC / "com/terminator364/kinlink/core/CockpitPrimaryActionPolicy.kt").read_text(encoding="utf-8")
MAIN_ACTIVITY = (SRC / "com/terminator364/kinlink/ui/MainActivity.kt").read_text(encoding="utf-8")
require(
    "MIN_SCORE_DELTA = 12" in EVIDENCE_TRACKER
    and "baselineScore" in EVIDENCE_TRACKER
    and "currentScore" in EVIDENCE_TRACKER,
    "candidate contract: Mobile Assist evidence does not require a meaningful passive score delta",
)
require(
    "InternetState.STALLED" in QUALITY_SCORE
    and "coerceIn(0, 100)" in QUALITY_SCORE,
    "candidate contract: passive quality score is incomplete or unbounded",
)
require(
    "RecoveryMode.OBSERVATION_ONLY" in COCKPIT_ACTION
    and "Transport.WIFI" in COCKPIT_ACTION
    and "Transport.CELLULAR" in COCKPIT_ACTION,
    "candidate contract: contextual cockpit action policy missing",
)
require(
    "Cela améliore l’observation, pas directement le débit" in MAIN_ACTIVITY,
    "candidate contract: UI overclaims Mobile Assist throughput improvement",
)

EVIDENCE_SAMPLING = (SRC / "com/terminator364/kinlink/core/MobileAssistEvidenceSamplingPolicy.kt").read_text(encoding="utf-8")
require(
    "MAX_SAMPLES_PER_ACTION = 3" in EVIDENCE_SAMPLING
    and "longArrayOf(21_000L, 42_000L, 65_000L)" in EVIDENCE_SAMPLING
    and "MobileAssistEvidenceSamplingPolicy.sampleDelaysMs" in SERVICE
    and "postDelayed" in SERVICE
    and "mobileAssistEvidenceHandler.removeCallbacksAndMessages(null)" in SERVICE,
    "candidate contract: bounded one-shot Mobile Assist evidence confirmation missing",
)

require(
    "ACTION_TRACK_MANUAL_MOBILE_ASSIST" in SERVICE
    and "ACTION_TRACK_MANUAL_MOBILE_ASSIST" in MAIN_ACTIVITY
    and "MOBILE_ASSIST_EVIDENCE_MANUAL_WINDOW_STARTED" in SERVICE,
    "candidate contract: manual Mobile Assist is detached from the proof loop",
)

require(
    EVIDENCE_TRACKER.count("@Synchronized") >= 2,
    "candidate contract: Mobile Assist evidence state is not serialized across callbacks and one-shot samples",
)

require(
    "AtomicLong(0L)" in SERVICE
    and "mobileAssistEvidenceGeneration.incrementAndGet()" in SERVICE
    and "mobileAssistEvidenceGeneration.get()" in SERVICE,
    "candidate contract: Mobile Assist one-shot generation fence is not thread-safe",
)

require(
    "@Volatile\n    private var latestTruth" in SERVICE,
    "candidate contract: service latestTruth is not safely published across callback/start-command threads",
)

LAYOUT = (ROOT / "app/src/main/res/layout/activity_main.xml").read_text(encoding="utf-8")
USER_EXPERIENCE = (SRC / "com/terminator364/kinlink/core/UserExperienceTruthPolicy.kt").read_text(encoding="utf-8")
require(
    'android:id="@+id/qualityProgress"' in LAYOUT
    and 'android:visibility="gone"' in LAYOUT.split('android:id="@+id/qualityProgress"', 1)[1][:500]
    and "UserExperienceTruthPolicy.assess(" in MAIN_ACTIVITY
    and "ACCESS_AVAILABLE_QUALITY_UNVERIFIED" in USER_EXPERIENCE,
    "candidate contract: user-facing quality truth policy is missing or pseudo-score is visible",
)

RELAPSE_GUARD = (SRC / "com/terminator364/kinlink/core/MobileAssistRelapseGuardPolicy.kt").read_text(encoding="utf-8")
require(
    "RECHECK_DELAY_MS = 5L * 60L * 1000L" in RELAPSE_GUARD
    and "MAX_RECHECKS_PER_SUSTAINED_EVENT = 1" in RELAPSE_GUARD,
    "candidate contract: bounded sustained-benefit relapse recheck missing",
)
require(
    "MOBILE_ASSIST_CONTINUOUS_REEVALUATION" in SERVICE
    and "scheduleMobileAssistRelapseRecheck()" in SERVICE
    and "mobileAssistRelapseGeneration" in SERVICE,
    "candidate contract: continuous Mobile Assist relapse re-evaluation path missing",
)

require(
    "MOBILE_ASSIST_EVIDENCE_RESOURCE_ABORT" in SERVICE
    and "MOBILE_ASSIST_RELAPSE_RESOURCE_ABORT" in SERVICE
    and "mobileAssistEvidenceResourceGuard.snapshot().constrained" in SERVICE,
    "candidate contract: Mobile Assist proof sampling ignores resource pressure",
)
require(
    "ACTION_MOBILE_ASSIST_EVIDENCE_UPDATED" in SERVICE
    and ".setPackage(packageName)" in SERVICE
    and "ContextCompat.RECEIVER_NOT_EXPORTED" in MAIN_ACTIVITY,
    "candidate contract: proof-card refresh is not package-scoped/non-exported",
)
require(
    '"Qualité · " + passiveScore.score' not in MAIN_ACTIVITY
    and "Expérience · " in MAIN_ACTIVITY
    and 'Indice technique interne : ${passiveScore.score}/100' in MAIN_ACTIVITY
    and "ne prouve pas la qualité ressentie" in MAIN_ACTIVITY,
    "candidate contract: Android passive score leaked back into user-facing quality claims",
)

EVIDENCE_SUMMARY = (SRC / "com/terminator364/kinlink/core/MobileAssistEvidenceSummaryPolicy.kt").read_text(encoding="utf-8")
require(
    "SUSTAINED_BETTER" in EVIDENCE_SUMMARY
    and "RELAPSING" in EVIDENCE_SUMMARY
    and "NO_CONFIRMED_BENEFIT" in EVIDENCE_SUMMARY
    and "corrélé" in EVIDENCE_SUMMARY
    and "rechute" in EVIDENCE_SUMMARY,
    "candidate contract: proof summary can hide relapse or overclaim causality",
)
require(
    "MobileAssistEvidenceSummaryPolicy.summarize(counts).label" in MAIN_ACTIVITY,
    "candidate contract: cockpit does not use tested proof summary",
)

require(
    "countExactActionSince(EVIDENCE_NO_BETTER, since)" in MOBILE_ASSIST
    and "countExactActionSince(EVIDENCE_RELAPSED, since)" in MOBILE_ASSIST
    and "countActionsSince(EVIDENCE_RELAPSED, since)" not in MOBILE_ASSIST,
    "candidate contract: late RELAPSED_AFTER_SUSTAINED may be miscounted as an ineffective early relapse",
)

require(
    "EXTRA_BASELINE_QUALITY" in SERVICE
    and "EXTRA_BASELINE_SCORE" in SERVICE
    and "EXTRA_BASELINE_OBSERVED_AT" in SERVICE
    and "startBaseline(" in EVIDENCE_TRACKER
    and "baselineQuality.name" in MAIN_ACTIVITY
    and "baselineScore" in MAIN_ACTIVITY
    and "baselineObservedAt" in MAIN_ACTIVITY,
    "candidate contract: manual Mobile Assist proof does not preserve the pre-action baseline",
)

require(
    re.search(
        r"private fun startMobileAssistEvidenceWindow\([\s\S]*?"
        r"mobileAssistRelapseGeneration\.incrementAndGet\(\)[\s\S]*?"
        r"mobileAssistEvidenceGeneration\.incrementAndGet\(\)",
        SERVICE,
    ) is not None,
    "candidate contract: a stale relapse callback can survive into a newer proof window",
)

require(
    "preActionTruth = ConnectivityTruthEngine.reduce(" in MAIN_ACTIVITY
    and "observedAtMillis = SystemClock.elapsedRealtime()" in MAIN_ACTIVITY
    and "baselineObservedAt = preActionTruth.observedAtMillis" in MAIN_ACTIVITY,
    "candidate contract: manual proof baseline can use stale callback state/time",
)
require(
    "Mobile Assist · améliorer maintenant" not in LAYOUT
    and 'android:maxLines="5"' not in LAYOUT,
    "candidate contract: compact cockpit can overclaim or clip continuous-care evidence",
)

require(
    'android:id="@+id/beforeScoreText"' in LAYOUT
    and 'android:id="@+id/nowScoreText"' in LAYOUT
    and 'android:id="@+id/deltaScoreText"' in LAYOUT
    and 'android:id="@+id/maintainedText"' in LAYOUT
    and 'android:id="@+id/evidenceText"' in LAYOUT,
    "candidate contract: before/now/delta/maintenance proof cockpit missing",
)
require(
    'android:id="@+id/versionText"' in LAYOUT
    and 'android:layout_width="match_parent"' in LAYOUT.split('android:id="@+id/versionText"', 1)[1][:400],
    "candidate contract: version/header can regress to narrow wrap-content compression",
)
require(
    "ContinuousControlPanelPolicy.build(" in MAIN_ACTIVITY
    and 'ledger.latestActionReceipt("MOBILE_ASSIST_EVIDENCE_")' in MAIN_ACTIVITY,
    "candidate contract: proof cockpit is not bound to latest durable evidence",
)
require(
    "reliabilityHumanLabel(" in MAIN_ACTIVITY
    and "Historique 24 h" in MAIN_ACTIVITY
    and "aucun gain causal confirmé" in MAIN_ACTIVITY,
    "candidate contract: current truth, historical burden, and improvement proof are not distinguished",
)

PROFILE_TUNING = (SRC / "com/terminator364/kinlink/core/AutopilotProfileControlPolicy.kt").read_text(encoding="utf-8")
require(
    "mobileAssistCooldownMs = 10L * 60L * 1000L" in PROFILE_TUNING
    and "mobileAssistCooldownMs = 5L * 60L * 1000L" in PROFILE_TUNING
    and "mobileAssistCooldownMs = 3L * 60L * 1000L" in PROFILE_TUNING
    and "mobileAssistMaxActionsPerHour = 8" in PROFILE_TUNING,
    "candidate contract: Autopilot modes do not materially tune bounded control",
)
require(
    "profileStore.current()" in SERVICE
    and "profile: AutopilotProfile" in MOBILE_ASSIST,
    "candidate contract: selected Autopilot mode is detached from background Mobile Assist",
)

require(
    "getOrDefault(tuning.mobileAssistMaxActionsPerHour)" in MOBILE_ASSIST,
    "candidate contract: Mobile Assist action-history failure can bypass profile cap",
)
