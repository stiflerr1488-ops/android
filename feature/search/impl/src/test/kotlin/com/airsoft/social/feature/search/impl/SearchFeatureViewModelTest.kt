package com.airsoft.social.feature.search.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchFeatureViewModelTest {
    @Test
    fun `search viewmodel selects category`() {
        val viewModel = SearchViewModel()

        viewModel.onAction(SearchAction.SelectCategory("Маркет"))

        assertEquals("Маркет", viewModel.uiState.value.selectedCategory)
    }
}
