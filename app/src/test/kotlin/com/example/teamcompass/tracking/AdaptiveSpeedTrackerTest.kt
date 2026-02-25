package com.example.teamcompass.tracking

import android.location.Location
import com.example.teamcompass.core.MovementState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdaptiveSpeedTrackerTest {

    @Test
    fun hysteresis_requires_min_time_before_transition() {
        val clock = MutableClock(nowMs = 10_000L)
        val tracker = AdaptiveSpeedTracker(nowMs = { clock.nowMs })

        val first = tracker.update(location(speedMps = 0f, accuracyM = 5f, timeMs = 10_000L))
        assertEquals(MovementState.STATIONARY, first)

        clock.nowMs = 11_000L
        val second = tracker.update(location(speedMps = 3f, accuracyM = 5f, timeMs = 11_000L))
        assertEquals(MovementState.STATIONARY, second)

        clock.nowMs = 17_000L
        val third = tracker.update(location(speedMps = 3f, accuracyM = 5f, timeMs = 17_000L))
        assertEquals(MovementState.WALKING_FAST, third)
    }

    @Test
    fun moving_average_prevents_single_spike_from_vehicle_state() {
        val clock = MutableClock(nowMs = 20_000L)
        val tracker = AdaptiveSpeedTracker(nowMs = { clock.nowMs })

        tracker.update(location(speedMps = 0f, accuracyM = 5f, timeMs = 20_000L))
        clock.nowMs = 26_000L
        tracker.update(location(speedMps = 0f, accuracyM = 5f, timeMs = 26_000L))
        clock.nowMs = 32_000L
        tracker.update(location(speedMps = 0f, accuracyM = 5f, timeMs = 32_000L))
        clock.nowMs = 38_000L
        val stateAfterSpike = tracker.update(location(speedMps = 10f, accuracyM = 5f, timeMs = 38_000L))

        assertNotEquals(MovementState.VEHICLE, stateAfterSpike)
    }

    @Test
    fun sustained_high_speed_reaches_vehicle_state() {
        val clock = MutableClock(nowMs = 30_000L)
        val tracker = AdaptiveSpeedTracker(nowMs = { clock.nowMs })

        tracker.update(location(speedMps = 8f, accuracyM = 5f, timeMs = 30_000L))
        clock.nowMs = 36_000L
        val state = tracker.update(location(speedMps = 8f, accuracyM = 5f, timeMs = 36_000L))

        assertEquals(MovementState.VEHICLE, state)
    }

    @Test
    fun distance_based_speed_with_zero_time_delta_stays_stationary() {
        val clock = MutableClock(nowMs = 40_000L)
        val tracker = AdaptiveSpeedTracker(nowMs = { clock.nowMs })

        val first = tracker.update(
            location(
                speedMps = -1f,
                accuracyM = 200f,
                latitude = 55.0,
                longitude = 37.0,
                timeMs = 40_000L,
            )
        )
        assertEquals(MovementState.STATIONARY, first)

        clock.nowMs = 46_000L
        val second = tracker.update(
            location(
                speedMps = -1f,
                accuracyM = 200f,
                latitude = 55.001,
                longitude = 37.001,
                timeMs = 40_000L,
            )
        )
        assertEquals(MovementState.STATIONARY, second)
    }

    private fun location(
        speedMps: Float,
        accuracyM: Float,
        timeMs: Long,
        latitude: Double = 55.0,
        longitude: Double = 37.0,
    ): Location {
        return Location("test").apply {
            this.latitude = latitude
            this.longitude = longitude
            accuracy = accuracyM
            speed = speedMps
            time = timeMs
        }
    }

    private data class MutableClock(
        var nowMs: Long,
    )
}
