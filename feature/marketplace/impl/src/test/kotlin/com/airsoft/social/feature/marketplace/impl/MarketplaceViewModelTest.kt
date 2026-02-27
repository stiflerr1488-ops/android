package com.airsoft.social.feature.marketplace.impl

import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewMarketplaceRepository
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MarketplaceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `maps repository listings into ui state`() = runTest {
        val viewModel = MarketplaceViewModel(
            PreviewMarketplaceRepository(DemoSocialRepositoryProvider.repository),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.listingsFeed.isNotEmpty())
        assertTrue(viewModel.uiState.value.myListings.isNotEmpty())
    }

    @Test
    fun `selecting filters updates state`() = runTest {
        val viewModel = MarketplaceViewModel(
            PreviewMarketplaceRepository(DemoSocialRepositoryProvider.repository),
        )

        viewModel.onAction(MarketplaceAction.SelectCategoryFilter("AEGs"))
        viewModel.onAction(MarketplaceAction.SelectQuickFilter("Shipping"))
        advanceUntilIdle()

        assertEquals("AEGs", viewModel.uiState.value.selectedCategoryFilter)
        assertEquals("Shipping", viewModel.uiState.value.selectedQuickFilter)
    }
}
