package com.airsoft.social.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewEventsRepositoryTest {

    private val repository = PreviewEventsRepository(DemoSocialRepositoryProvider.repository)

    @Test
    fun `observe events returns seeded list`() = runTest {
        val events = repository.observeEvents().first()

        assertTrue(events.isNotEmpty())
        assertEquals("night-raid-north", events.first().id)
    }

    @Test
    fun `observe event returns item by id`() = runTest {
        val event = repository.observeEvent("night-raid-north").first()

        assertNotNull(event)
        assertEquals("night-raid-north", event?.id)
    }
}
