package com.example.teamcompass.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseTeamRepositoryStateCellCacheTest {

    @Test
    fun prunes_when_limit_exceeded() {
        val cache = linkedMapOf(
            "u1" to StateCellEntry(cellId = "c1", lastUpdatedMs = 1L),
            "u2" to StateCellEntry(cellId = "c2", lastUpdatedMs = 2L),
            "u3" to StateCellEntry(cellId = "c3", lastUpdatedMs = 3L),
            "u4" to StateCellEntry(cellId = "c4", lastUpdatedMs = 4L),
            "u5" to StateCellEntry(cellId = "c5", lastUpdatedMs = 5L),
            "u6" to StateCellEntry(cellId = "c6", lastUpdatedMs = 6L),
        )

        val removed = pruneStateCellCacheEntries(
            cache = cache,
            maxEntries = 5,
            pruneToEntries = 4,
        )

        assertEquals(2, removed)
        assertEquals(4, cache.size)
        assertFalse(cache.containsKey("u1"))
        assertFalse(cache.containsKey("u2"))
        assertTrue(cache.containsKey("u6"))
    }

    @Test
    fun does_not_prune_when_under_limit() {
        val cache = linkedMapOf(
            "u1" to StateCellEntry(cellId = "c1", lastUpdatedMs = 1L),
            "u2" to StateCellEntry(cellId = "c2", lastUpdatedMs = 2L),
            "u3" to StateCellEntry(cellId = "c3", lastUpdatedMs = 3L),
        )

        val removed = pruneStateCellCacheEntries(
            cache = cache,
            maxEntries = 5,
            pruneToEntries = 4,
        )

        assertEquals(0, removed)
        assertEquals(3, cache.size)
    }
}
