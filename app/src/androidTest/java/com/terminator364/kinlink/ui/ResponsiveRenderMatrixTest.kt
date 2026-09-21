package com.terminator364.kinlink.ui

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.terminator364.kinlink.R
import com.terminator364.kinlink.core.ConnectivityTruthEngine
import com.terminator364.kinlink.core.MobileBudgetSnapshot
import com.terminator364.kinlink.core.NetworkObserver
import com.terminator364.kinlink.core.NetworkTruth
import com.terminator364.kinlink.core.RecoveryMode
import com.terminator364.kinlink.core.RecoveryModeStore
import com.terminator364.kinlink.core.ResourceGuardSnapshot
import com.terminator364.kinlink.core.Transport
import com.terminator364.kinlink.data.StabilityWindow
import java.io.File
import java.io.FileOutputStream
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResponsiveRenderMatrixTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext
    private val matrixId =
        InstrumentationRegistry.getArguments().getString("matrixId") ?: "unknown-matrix"

    @Before
    fun resetMode() {
        RecoveryModeStore(targetContext).set(RecoveryMode.AUTOMATIC)
    }

    @After
    fun restoreMode() {
        RecoveryModeStore(targetContext).set(RecoveryMode.AUTOMATIC)
    }

    @Test
    fun renderedStateMatrixHasNoResponsiveRegression() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> stopLiveObserver(activity) }
            instrumentation.waitForIdleSync()

            val wifiGood = ConnectivityTruthEngine.reduce(
                ConnectivityTruthEngine.Snapshot(
                    transport = Transport.WIFI,
                    validated = true,
                    interfaceName = "wlan0",
                    downstreamKbps = 28_000,
                    upstreamKbps = 8_000,
                    dnsServerCount = 2,
                    hasIpv4Address = true,
                    hasIpv4DefaultRoute = true,
                    signalStrengthDbm = -52
                )
            )
            val wifiDegraded = ConnectivityTruthEngine.reduce(
                ConnectivityTruthEngine.Snapshot(
                    transport = Transport.WIFI,
                    validated = false,
                    interfaceName = "wlan0",
                    downstreamKbps = 320,
                    upstreamKbps = 96,
                    dnsServerCount = 1,
                    hasIpv4Address = true,
                    hasIpv4DefaultRoute = true,
                    signalStrengthDbm = -84,
                    androidNotCongested = false
                )
            )
            val cellularDegraded = ConnectivityTruthEngine.reduce(
                ConnectivityTruthEngine.Snapshot(
                    transport = Transport.CELLULAR,
                    validated = true,
                    metered = true,
                    downstreamKbps = 640,
                    upstreamKbps = 160,
                    dnsServerCount = 2,
                    hasIpv4Address = true,
                    hasIpv4DefaultRoute = true,
                    signalStrengthDbm = -112,
                    androidNotCongested = false
                )
            )
            val offline = ConnectivityTruthEngine.reduce(ConnectivityTruthEngine.Snapshot())

            renderAndCapture(scenario, "wifi-good", wifiGood)
            renderAndCapture(scenario, "wifi-degraded", wifiDegraded)
            renderAndCapture(scenario, "cellular-degraded", cellularDegraded)
            renderAndCapture(scenario, "offline", offline)

            RecoveryModeStore(targetContext).set(RecoveryMode.OBSERVATION_ONLY)
            renderAndCapture(scenario, "safe-mode", wifiGood)
            scenario.onActivity { activity ->
                val eyebrow = activity.findViewById<TextView>(R.id.heroEyebrow)
                assertTrue("safe-mode state was not rendered", eyebrow.text.toString().contains("MODE SÛR"))
            }

            RecoveryModeStore(targetContext).set(RecoveryMode.AUTOMATIC)
            val constrainedResources = ResourceGuardSnapshot(
                powerSaveMode = true,
                thermalModerateOrWorse = false,
                lowMemory = false
            )
            renderAndCapture(
                scenario,
                "resource-constrained",
                cellularDegraded,
                constrainedResources
            )
            scenario.onActivity { activity ->
                val evidence = activity.findViewById<TextView>(R.id.evidenceText)
                assertTrue(
                    "resource-constrained state did not expose its protection reason",
                    evidence.text.toString().startsWith("Protection ·")
                )
            }
        }
    }

    private fun renderAndCapture(
        scenario: ActivityScenario<MainActivity>,
        stateName: String,
        truth: NetworkTruth,
        resourceSnapshot: ResourceGuardSnapshot? = null
    ) {
        scenario.onActivity { activity ->
            invokeRender(activity, truth, resourceSnapshot)
            val root = activity.findViewById<ScrollView>(R.id.rootScroll)
            root.fullScroll(View.FOCUS_UP)
        }
        instrumentation.waitForIdleSync()

        scenario.onActivity { activity ->
            assertResponsive(activity, stateName)
        }
        capture("${stateName}-top")

        scenario.onActivity { activity ->
            activity.findViewById<ScrollView>(R.id.rootScroll).fullScroll(View.FOCUS_DOWN)
        }
        instrumentation.waitForIdleSync()
        capture("${stateName}-bottom")

        scenario.onActivity { activity ->
            activity.findViewById<ScrollView>(R.id.rootScroll).fullScroll(View.FOCUS_UP)
        }
        instrumentation.waitForIdleSync()
    }

    private fun invokeRender(
        activity: MainActivity,
        truth: NetworkTruth,
        resourceSnapshot: ResourceGuardSnapshot? = null
    ) {
        val budgetField = MainActivity::class.java.getDeclaredField("latestBudget").apply {
            isAccessible = true
        }
        val budget = budgetField.get(activity) as MobileBudgetSnapshot
        val method = MainActivity::class.java.getDeclaredMethod(
            "render",
            NetworkTruth::class.java,
            MobileBudgetSnapshot::class.java,
            StabilityWindow::class.java,
            ResourceGuardSnapshot::class.java
        ).apply { isAccessible = true }
        method.invoke(activity, truth, budget, null, resourceSnapshot)
    }

    private fun stopLiveObserver(activity: MainActivity) {
        val field = MainActivity::class.java.getDeclaredField("observer").apply {
            isAccessible = true
        }
        (field.get(activity) as NetworkObserver).stop()
    }

    private fun assertResponsive(activity: MainActivity, stateName: String) {
        val root = activity.findViewById<ScrollView>(R.id.rootScroll)
        assertTrue("${stateName}: root has no width", root.width > 0)

        val minReadableWidth = dp(activity, 48)
        walkVisible(root) { view ->
            if (view is TextView) {
                val text = view.text?.toString().orEmpty()
                if (text.length >= 8) {
                    assertTrue(
                        "${stateName}: suspicious one-character-column width for '${text}': ${view.width}px",
                        view.width >= minReadableWidth
                    )
                }
                val layout = view.layout
                if (layout != null && text.isNotBlank() && layout.lineCount > 0) {
                    for (line in 0 until layout.lineCount) {
                        assertTrue(
                            "${stateName}: ellipsized text '${text}'",
                            layout.getEllipsisCount(line) == 0
                        )
                    }
                    val lastEnd = layout.getLineEnd(layout.lineCount - 1)
                    assertTrue(
                        "${stateName}: clipped text '${text}' (${lastEnd}/${text.length})",
                        lastEnd >= text.length
                    )
                }
            }
            if (view is ViewGroup) {
                assertNoSiblingOverlap(view, stateName)
            }
        }

        for (id in intArrayOf(
            R.id.wifiDoctorButton,
            R.id.mobileAssistButton,
            R.id.modeConservativeButton,
            R.id.modeBalancedButton,
            R.id.modeMaxButton,
            R.id.safeModeButton,
            R.id.budgetButton,
            R.id.incidentMarkerButton
        )) {
            val view = activity.findViewById<View>(id)
            if (view.visibility == View.VISIBLE) {
                assertTrue(
                    "${stateName}: tap target height < 48dp for id=${id}",
                    view.height >= dp(activity, 48)
                )
                assertTrue(
                    "${stateName}: tap target width < 48dp for id=${id}",
                    view.width >= dp(activity, 48)
                )
            }
        }

        val version = activity.findViewById<TextView>(R.id.versionText)
        assertTrue(
            "${stateName}: version/header width collapsed",
            version.width >= (root.width * 0.75f).toInt()
        )
        assertFalse(
            "${stateName}: technical details expanded by default",
            activity.findViewById<View>(R.id.detailText).visibility == View.VISIBLE
        )
    }

    private fun assertNoSiblingOverlap(parent: ViewGroup, stateName: String) {
        val visible = (0 until parent.childCount)
            .map(parent::getChildAt)
            .filter { it.visibility == View.VISIBLE && it.width > 0 && it.height > 0 }
        for (i in visible.indices) {
            for (j in i + 1 until visible.size) {
                val a = Rect()
                val b = Rect()
                visible[i].getHitRect(a)
                visible[j].getHitRect(b)
                assertFalse(
                    "${stateName}: sibling overlap in ${parent.javaClass.simpleName}: ${a} vs ${b}",
                    Rect.intersects(a, b)
                )
            }
        }
    }

    private fun walkVisible(view: View, block: (View) -> Unit) {
        if (view.visibility != View.VISIBLE) return
        block(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                walkVisible(view.getChildAt(i), block)
            }
        }
    }

    private fun capture(name: String) {
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
            ?: throw AssertionError("Unable to capture screenshot for ${name}")
        val dir = File(targetContext.getExternalFilesDir(null), "ui-proof/${matrixId}")
        assertTrue("Cannot create screenshot directory ${dir.absolutePath}", dir.mkdirs() || dir.isDirectory)
        val out = File(dir, "${matrixId}-${name}.png")
        FileOutputStream(out).use { stream ->
            assertTrue("PNG compression failed for ${out.name}", bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        bitmap.recycle()
        assertTrue("Screenshot missing: ${out.absolutePath}", out.isFile && out.length() > 0L)
    }

    private fun dp(activity: MainActivity, value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()
}
