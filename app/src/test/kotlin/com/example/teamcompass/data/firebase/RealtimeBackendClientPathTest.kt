package com.example.teamcompass.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Test

class RealtimeBackendClientPathTest {

    @Test
    fun splitRealtimePath_blank_returns_empty() {
        assertEquals(emptyList<String>(), splitRealtimePath("   "))
    }

    @Test
    fun splitRealtimePath_normalized_segments_without_empty_parts() {
        assertEquals(
            listOf("teams", "123456", "meta"),
            splitRealtimePath("/teams//123456///meta/"),
        )
    }
}
