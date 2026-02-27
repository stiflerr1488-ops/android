package com.airsoft.social.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewMarketplaceRepositoryTest {

    private val repository = PreviewMarketplaceRepository(DemoSocialRepositoryProvider.repository)

    @Test
    fun `observe feed returns seeded listings`() = runTest {
        val listings = repository.observeMarketplaceFeed().first()

        assertTrue(listings.isNotEmpty())
        assertEquals("m4a1-cyma-3mags", listings.first().id)
    }

    @Test
    fun `observe my listings returns self seller items`() = runTest {
        val listings = repository.observeMyListings().first()

        assertTrue(listings.isNotEmpty())
        assertTrue(listings.all { it.sellerId == MarketplaceRepository.DEFAULT_SELLER_ID })
    }

    @Test
    fun `observe listing returns item by id`() = runTest {
        val listing = repository.observeListing("m4a1-cyma-3mags").first()

        assertNotNull(listing)
        assertEquals("m4a1-cyma-3mags", listing?.id)
    }
}
