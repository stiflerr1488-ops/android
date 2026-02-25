package com.example.teamcompass.tracking

import com.example.teamcompass.core.PlayerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingRuntimePolicyTest {

    @Test
    fun watchdog_restart_allowed_when_no_previous_restart() {
        assertTrue(TrackingRuntime.isWatchdogRestartAllowed(lastRestartAtMs = 0L, nowMs = 1_000L))
    }

    @Test
    fun watchdog_restart_blocked_inside_suppression_window() {
        assertFalse(
            TrackingRuntime.isWatchdogRestartAllowed(
                lastRestartAtMs = 100_000L,
                nowMs = 180_000L,
            )
        )
    }

    @Test
    fun watchdog_restart_allowed_after_suppression_window() {
        assertTrue(
            TrackingRuntime.isWatchdogRestartAllowed(
                lastRestartAtMs = 100_000L,
                nowMs = 190_000L,
            )
        )
    }

    @Test
    fun decideWatchdogAction_without_location_returns_no_action() {
        assertEquals(
            TrackingRuntime.WatchdogAction.NO_ACTION,
            TrackingRuntime.decideWatchdogAction(
                lastLocationAtMs = 0L,
                lastRestartAtMs = 0L,
                nowMs = 50_000L,
            )
        )
    }

    @Test
    fun decideWatchdogAction_recent_location_returns_no_action() {
        assertEquals(
            TrackingRuntime.WatchdogAction.NO_ACTION,
            TrackingRuntime.decideWatchdogAction(
                lastLocationAtMs = 10_000L,
                lastRestartAtMs = 0L,
                nowMs = 50_000L,
            )
        )
    }

    @Test
    fun decideWatchdogAction_stale_location_without_recent_restart_returns_restart() {
        assertEquals(
            TrackingRuntime.WatchdogAction.RESTART,
            TrackingRuntime.decideWatchdogAction(
                lastLocationAtMs = 1_000L,
                lastRestartAtMs = 0L,
                nowMs = 60_000L,
            )
        )
    }

    @Test
    fun decideWatchdogAction_repeated_stagnation_in_short_window_returns_throttled() {
        assertEquals(
            TrackingRuntime.WatchdogAction.THROTTLED,
            TrackingRuntime.decideWatchdogAction(
                lastLocationAtMs = 1_000L,
                lastRestartAtMs = 100_000L,
                nowMs = 160_000L,
            )
        )
    }

    @Test
    fun resolveSendInterval_dead_mode_forces_sixty_seconds() {
        assertEquals(
            60_000L,
            TrackingRuntime.resolveSendIntervalMs(
                mode = PlayerMode.DEAD,
                anchored = false,
                adaptiveIntervalMs = 5_000L,
            )
        )
    }

    @Test
    fun resolveSendInterval_anchored_mode_triples_and_caps() {
        assertEquals(
            60_000L,
            TrackingRuntime.resolveSendIntervalMs(
                mode = PlayerMode.GAME,
                anchored = true,
                adaptiveIntervalMs = 25_000L,
            )
        )
        assertEquals(
            18_000L,
            TrackingRuntime.resolveSendIntervalMs(
                mode = PlayerMode.GAME,
                anchored = true,
                adaptiveIntervalMs = 6_000L,
            )
        )
    }

    @Test
    fun resolveMinDistance_anchored_is_tripled() {
        assertEquals(30.0, TrackingRuntime.resolveMinDistanceMeters(anchored = true, adaptiveMinDistanceMeters = 10.0), 0.0001)
        assertEquals(10.0, TrackingRuntime.resolveMinDistanceMeters(anchored = false, adaptiveMinDistanceMeters = 10.0), 0.0001)
    }

    @Test
    fun applyIntervalJitter_isDeterministicWithinSameBucket() {
        val first = TrackingRuntime.applyIntervalJitter(
            intervalMs = 10_000L,
            uid = "alpha",
            nowMs = 50_000L,
        )
        val second = TrackingRuntime.applyIntervalJitter(
            intervalMs = 10_000L,
            uid = "alpha",
            nowMs = 59_999L,
        )
        assertEquals(first, second)
    }

    @Test
    fun applyIntervalJitter_staysWithinConfiguredBounds() {
        val base = 10_000L
        val lower = 9_000L
        val upper = 11_000L
        val a = TrackingRuntime.applyIntervalJitter(base, uid = "alpha", nowMs = 10_000L)
        val b = TrackingRuntime.applyIntervalJitter(base, uid = "alpha", nowMs = 110_000L)

        assertTrue(a in lower..upper)
        assertTrue(b in lower..upper)
    }

    @Test
    fun shouldSendState_triggers_for_force_interval_or_distance() {
        assertTrue(
            TrackingRuntime.shouldSendState(
                shouldForce = true,
                lastSentMs = 2000L,
                nowMs = 2500L,
                intervalMs = 10_000L,
                movedDistanceMeters = 0.5,
                minDistanceMeters = 10.0,
            )
        )
        assertTrue(
            TrackingRuntime.shouldSendState(
                shouldForce = false,
                lastSentMs = 0L,
                nowMs = 2500L,
                intervalMs = 10_000L,
                movedDistanceMeters = 0.5,
                minDistanceMeters = 10.0,
            )
        )
        assertTrue(
            TrackingRuntime.shouldSendState(
                shouldForce = false,
                lastSentMs = 2000L,
                nowMs = 13_000L,
                intervalMs = 10_000L,
                movedDistanceMeters = 0.5,
                minDistanceMeters = 10.0,
            )
        )
        assertTrue(
            TrackingRuntime.shouldSendState(
                shouldForce = false,
                lastSentMs = 2000L,
                nowMs = 2500L,
                intervalMs = 10_000L,
                movedDistanceMeters = 12.0,
                minDistanceMeters = 10.0,
            )
        )
    }

    @Test
    fun shouldSendState_false_when_no_send_trigger() {
        assertFalse(
            TrackingRuntime.shouldSendState(
                shouldForce = false,
                lastSentMs = 2000L,
                nowMs = 2500L,
                intervalMs = 10_000L,
                movedDistanceMeters = 1.0,
                minDistanceMeters = 10.0,
            )
        )
    }

    @Test
    fun shouldResetLastSentAfterMove_requires_game_and_previous_anchor() {
        assertTrue(
            TrackingRuntime.shouldResetLastSentAfterMove(
                moved = true,
                wasAnchored = true,
                mode = PlayerMode.GAME,
            )
        )
        assertFalse(
            TrackingRuntime.shouldResetLastSentAfterMove(
                moved = true,
                wasAnchored = false,
                mode = PlayerMode.GAME,
            )
        )
        assertFalse(
            TrackingRuntime.shouldResetLastSentAfterMove(
                moved = true,
                wasAnchored = true,
                mode = PlayerMode.DEAD,
            )
        )
    }
}
