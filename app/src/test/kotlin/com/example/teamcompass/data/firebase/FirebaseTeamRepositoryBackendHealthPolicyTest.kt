package com.example.teamcompass.data.firebase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseTeamRepositoryBackendHealthPolicyTest {

    @Test
    fun connected_with_zero_probe_failures_is_available() {
        assertTrue(
            computeBackendReachabilitySample(
                connected = true,
                consecutiveProbeFailures = 0,
                failureThreshold = 2,
            )
        )
    }

    @Test
    fun connected_below_failure_threshold_remains_available() {
        assertTrue(
            computeBackendReachabilitySample(
                connected = true,
                consecutiveProbeFailures = 1,
                failureThreshold = 2,
            )
        )
    }

    @Test
    fun connected_at_failure_threshold_becomes_unavailable() {
        assertFalse(
            computeBackendReachabilitySample(
                connected = true,
                consecutiveProbeFailures = 2,
                failureThreshold = 2,
            )
        )
    }

    @Test
    fun disconnected_is_always_unavailable() {
        assertFalse(
            computeBackendReachabilitySample(
                connected = false,
                consecutiveProbeFailures = 0,
                failureThreshold = 2,
            )
        )
    }

    @Test
    fun failure_threshold_is_clamped_to_at_least_one() {
        assertFalse(
            computeBackendReachabilitySample(
                connected = true,
                consecutiveProbeFailures = 1,
                failureThreshold = 0,
            )
        )
    }

    @Test
    fun permission_denied_probe_failure_is_not_counted() {
        assertFalse(
            shouldCountBackendProbeFailure(
                IllegalStateException("Permission denied for path .info/serverTimeOffset"),
            )
        )
    }

    @Test
    fun network_probe_failure_is_counted() {
        assertTrue(
            shouldCountBackendProbeFailure(
                IllegalStateException("network timeout"),
            )
        )
    }
}
