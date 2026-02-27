package com.airsoft.social.feature.support.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportScreensViewModelTest {
    @Test
    fun support_viewmodel_selects_topic() {
        val viewModel = SupportViewModel()

        viewModel.onAction(SupportAction.SelectTopic("TopicX"))

        assertEquals("TopicX", viewModel.uiState.value.selectedTopic)
    }

    @Test
    fun support_tickets_viewmodel_cycles_queue() {
        val viewModel = SupportTicketsViewModel()

        val initial = viewModel.uiState.value.selectedQueue
        viewModel.onAction(SupportTicketsAction.CycleQueue)

        assertTrue(viewModel.uiState.value.selectedQueue != initial)
        assertTrue(viewModel.uiState.value.ticketRows.isNotEmpty())
    }

    @Test
    fun support_ticket_detail_viewmodel_loads_and_cycles_tab() {
        val viewModel = SupportTicketDetailViewModel()

        viewModel.load("ticket-201")
        val initial = viewModel.uiState.value.selectedTab
        viewModel.onAction(SupportTicketDetailAction.CycleTab)

        assertEquals("ticket-201", viewModel.uiState.value.ticketId)
        assertTrue(viewModel.uiState.value.summaryRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.chatRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedTab != initial)
    }

    @Test
    fun support_chat_viewmodel_cycles_mode() {
        val viewModel = SupportChatViewModel()

        val initial = viewModel.uiState.value.selectedMode
        viewModel.onAction(SupportChatAction.CycleMode)

        assertTrue(viewModel.uiState.value.selectedMode != initial)
        assertTrue(viewModel.uiState.value.chatRows.isNotEmpty())
    }

    @Test
    fun support_faq_viewmodel_cycles_category() {
        val viewModel = SupportFaqViewModel()

        val initial = viewModel.uiState.value.selectedCategory
        viewModel.onAction(SupportFaqAction.CycleCategory)

        assertTrue(viewModel.uiState.value.selectedCategory != initial)
        assertTrue(viewModel.uiState.value.articleRows.isNotEmpty())
    }

    @Test
    fun about_viewmodel_toggles_build_channel() {
        val viewModel = AboutViewModel()

        assertEquals("Dev", viewModel.uiState.value.buildChannel)
        viewModel.onAction(AboutAction.ToggleBuildChannel)
        assertEquals("QA", viewModel.uiState.value.buildChannel)
    }
}
