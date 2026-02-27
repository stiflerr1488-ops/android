package com.airsoft.social.feature.events.impl

import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewEventsRepository
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `maps repository events into ui state`() = runTest {
        val viewModel = EventsViewModel(
            PreviewEventsRepository(DemoSocialRepositoryProvider.repository),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.upcomingGames.isNotEmpty())
        assertTrue(viewModel.uiState.value.summaryMetrics.isNotEmpty())
    }

    @Test
    fun `select filter updates state`() = runTest {
        val viewModel = EventsViewModel(
            PreviewEventsRepository(DemoSocialRepositoryProvider.repository),
        )

        viewModel.onAction(EventsAction.SelectFilter("Nearby"))
        advanceUntilIdle()

        assertEquals("Nearby", viewModel.uiState.value.selectedFilter)
    }
}
