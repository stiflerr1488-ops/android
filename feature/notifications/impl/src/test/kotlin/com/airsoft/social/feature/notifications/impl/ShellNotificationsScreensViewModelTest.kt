package com.airsoft.social.feature.notifications.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class ShellNotificationsScreensViewModelTest {
    @Test
    fun `notifications viewmodel selects filter`() {
        val viewModel = NotificationsViewModel()

        viewModel.onAction(NotificationsAction.SelectFilter("FilterX"))

        assertEquals("FilterX", viewModel.uiState.value.selectedFilter)
    }
}
