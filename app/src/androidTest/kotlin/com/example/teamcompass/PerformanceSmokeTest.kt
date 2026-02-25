package com.example.teamcompass

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class PerformanceSmokeTest {

    @Test
    fun launchTime_underReasonableBudget() {
        val startMs = SystemClock.elapsedRealtime()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val started = CountDownLatch(1)
            scenario.onActivity { started.countDown() }
            assertTrue("MainActivity did not start in time", started.await(15, TimeUnit.SECONDS))
        }
        val elapsedMs = SystemClock.elapsedRealtime() - startMs
        assertTrue("Launch time too slow: ${elapsedMs}ms", elapsedMs < 15_000L)
    }

    @Test
    fun heapUsage_afterLaunch_withinBudget() {
        var usedMb = Long.MAX_VALUE
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                val rt = Runtime.getRuntime()
                usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L)
            }
        }
        assertTrue("Unexpected heap usage after launch: ${usedMb}MB", usedMb in 1L..1024L)
    }
}

