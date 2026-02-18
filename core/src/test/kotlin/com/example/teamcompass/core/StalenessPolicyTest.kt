package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals

class StalenessPolicyTest {
    @Test
    fun `staleness buckets match mvp`() {
        assertEquals(Staleness.FRESH, StalenessPolicy.classify(20))
        assertEquals(Staleness.SUSPECT, StalenessPolicy.classify(21))
        assertEquals(Staleness.SUSPECT, StalenessPolicy.classify(60))
        assertEquals(Staleness.STALE, StalenessPolicy.classify(61))
        assertEquals(Staleness.STALE, StalenessPolicy.classify(120))
        assertEquals(Staleness.HIDDEN, StalenessPolicy.classify(121))
    }
}
