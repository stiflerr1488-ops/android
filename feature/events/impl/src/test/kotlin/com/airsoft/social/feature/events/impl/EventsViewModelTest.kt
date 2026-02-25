package com.airsoft.social.feature.events.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class EventsViewModelTest {

    @Test
    fun `select filter updates state`() {
        val viewModel = EventsViewModel()

        viewModel.onAction(EventsAction.SelectFilter("Nearby"))

        assertEquals("Nearby", viewModel.uiState.value.selectedFilter)
    }
}

