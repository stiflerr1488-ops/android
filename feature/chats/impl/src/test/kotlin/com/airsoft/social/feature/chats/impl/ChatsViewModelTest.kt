package com.airsoft.social.feature.chats.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatsViewModelTest {

    @Test
    fun `select filter updates state`() {
        val viewModel = ChatsViewModel()

        viewModel.onAction(ChatsAction.SelectFilter("Unread"))

        assertEquals("Unread", viewModel.uiState.value.selectedFilter)
    }
}

