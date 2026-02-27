package com.airsoft.social.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewTeamsRepositoryTest {

    private val repository = PreviewTeamsRepository(DemoSocialRepositoryProvider.repository)

    @Test
    fun `observe my team returns seeded ew team`() = runTest {
        val team = repository.observeMyTeam().first()

        assertNotNull(team)
        assertEquals("ew-easy-winner", team?.id)
    }

    @Test
    fun `observe roster returns members for team`() = runTest {
        val roster = repository.observeRoster("ew-easy-winner").first()

        assertTrue(roster.isNotEmpty())
        assertTrue(roster.all { it.teamId == "ew-easy-winner" })
    }

    @Test
    fun `observe recruiting feed returns seeded posts`() = runTest {
        val feed = repository.observeRecruitingFeed().first()

        assertTrue(feed.isNotEmpty())
        assertTrue(feed.first().title.isNotBlank())
    }
}
