package com.example.teamcompass

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainActivityUiPolicyTest {
    @Test
    fun `stale marks align with shared staleness policy`() {
        assertEquals("", MainActivityUiPolicy.staleMark(20))
        assertEquals(" (сомн)", MainActivityUiPolicy.staleMark(21))
        assertEquals(" (сомн)", MainActivityUiPolicy.staleMark(60))
        assertEquals(" (старые)", MainActivityUiPolicy.staleMark(61))
        assertEquals(" (старые)", MainActivityUiPolicy.staleMark(120))
        assertEquals("", MainActivityUiPolicy.staleMark(121))
    }

    @Test
    fun `hidden classification works for all stale scenario`() {
        val ages = listOf(121L, 130L, 500L)
        assertTrue(ages.all { MainActivityUiPolicy.isHidden(it) })
    }
}
