package com.airsoft.social.feature.marketplace.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class MarketplaceViewModelTest {

    @Test
    fun `selecting filters updates state`() {
        val viewModel = MarketplaceViewModel()

        viewModel.onAction(MarketplaceAction.SelectCategoryFilter("AEGs"))
        viewModel.onAction(MarketplaceAction.SelectQuickFilter("Shipping"))

        assertEquals("AEGs", viewModel.uiState.value.selectedCategoryFilter)
        assertEquals("Shipping", viewModel.uiState.value.selectedQuickFilter)
    }
}

