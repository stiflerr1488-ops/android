package com.airsoft.social.feature.chats.impl

import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `maps repository threads and players into ui state`() = runTest {
        val viewModel = ChatsViewModel(FakeChatsRepository())

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.activeDialogs.isNotEmpty())
        assertTrue(viewModel.uiState.value.nearbyPlayers.isNotEmpty())
        assertEquals("Команда [EW] EASY WINNER", viewModel.uiState.value.activeDialogs.first().title)
    }

    @Test
    fun `changes selected filter`() = runTest {
        val viewModel = ChatsViewModel(FakeChatsRepository())

        viewModel.onAction(ChatsAction.SelectFilter("Непрочитанные"))
        advanceUntilIdle()

        assertEquals("Непрочитанные", viewModel.uiState.value.selectedFilter)
    }
}
