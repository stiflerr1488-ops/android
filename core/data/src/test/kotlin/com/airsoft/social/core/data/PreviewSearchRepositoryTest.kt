package com.airsoft.social.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewSearchRepositoryTest {

    private val repository = PreviewSearchRepository(DemoSocialRepositoryProvider.repository)

    @Test
    fun `returns seeded players teams events and listings`() = runTest {
        assertTrue(repository.observeUsers().first().isNotEmpty())
        assertTrue(repository.observeTeams().first().isNotEmpty())
        assertTrue(repository.observeEvents().first().isNotEmpty())
        assertTrue(repository.observeMarketplaceListings().first().isNotEmpty())
    }

    @Test
    fun `returns seeded recent and saved queries`() = runTest {
        assertTrue(repository.observeRecentQueries().first().isNotEmpty())
        assertTrue(repository.observeSavedSearches().first().isNotEmpty())
    }
}

