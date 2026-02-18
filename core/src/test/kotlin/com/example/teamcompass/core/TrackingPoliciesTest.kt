package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals

class TrackingPoliciesTest {
    @Test
    fun `game and silent policies are as specified`() {
        assertEquals(3_000, TrackingPolicies.game.minIntervalMs)
        assertEquals(10.0, TrackingPolicies.game.minDistanceMeters)
        assertEquals(10_000, TrackingPolicies.silent.minIntervalMs)
        assertEquals(30.0, TrackingPolicies.silent.minDistanceMeters)
    }

    @Test
    fun `retry backoff capped at 60 sec`() {
        assertEquals(1, RetryBackoff.delayForAttemptSec(1))
        assertEquals(2, RetryBackoff.delayForAttemptSec(2))
        assertEquals(4, RetryBackoff.delayForAttemptSec(3))
        assertEquals(60, RetryBackoff.delayForAttemptSec(12))
    }
}
