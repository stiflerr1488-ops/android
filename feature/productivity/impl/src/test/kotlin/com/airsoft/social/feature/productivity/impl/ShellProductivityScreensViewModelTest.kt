package com.airsoft.social.feature.productivity.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellProductivityScreensViewModelTest {
    @Test
    fun `dashboard viewmodel cycles focus`() {
        val viewModel = DashboardViewModel()

        val initialFocus = viewModel.uiState.value.selectedFocus
        viewModel.onAction(DashboardAction.CycleFocus)

        assertTrue(viewModel.uiState.value.selectedFocus != initialFocus)
        assertTrue(viewModel.uiState.value.upcomingGameRows.isNotEmpty())
    }

    @Test
    fun `announcements viewmodel cycles channel and toggles pinned`() {
        val viewModel = AnnouncementsViewModel()

        val initialChannel = viewModel.uiState.value.selectedChannel
        val initialPinnedOnly = viewModel.uiState.value.pinnedOnly
        viewModel.onAction(AnnouncementsAction.CycleChannel)
        viewModel.onAction(AnnouncementsAction.TogglePinnedOnly)

        assertTrue(viewModel.uiState.value.selectedChannel != initialChannel)
        assertTrue(viewModel.uiState.value.pinnedOnly != initialPinnedOnly)
    }

    @Test
    fun `announcement detail viewmodel loads and cycles section`() {
        val viewModel = AnnouncementDetailViewModel()

        viewModel.load("safety-rules-update")
        val initialSection = viewModel.uiState.value.selectedSection
        viewModel.onAction(AnnouncementDetailAction.CycleSection)

        assertEquals("safety-rules-update", viewModel.uiState.value.announcementId)
        assertTrue(viewModel.uiState.value.summaryRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedSection != initialSection)
    }
}
