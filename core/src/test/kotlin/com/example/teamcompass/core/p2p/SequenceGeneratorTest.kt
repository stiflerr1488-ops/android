package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertEquals

class SequenceGeneratorTest {

    @Test
    fun next_increments() {
        val generator = SequenceGenerator(initialValue = 5)
        assertEquals(5, generator.next())
        assertEquals(6, generator.next())
        assertEquals(7, generator.next())
    }

    @Test
    fun next_wrapsAtIntMax() {
        val generator = SequenceGenerator(initialValue = Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, generator.next())
        assertEquals(0, generator.next())
        assertEquals(1, generator.next())
    }
}
