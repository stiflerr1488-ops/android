package com.airsoft.social.feature.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellPlaceholderScreensViewModelTest {
    @Test
    fun `settings viewmodel toggles push and cycles theme`() {
        val viewModel = SettingsViewModel()

        assertTrue(viewModel.uiState.value.pushEnabled)
        assertEquals("System", viewModel.uiState.value.selectedTheme)

        viewModel.onAction(SettingsAction.TogglePush)
        viewModel.onAction(SettingsAction.CycleTheme)

        assertFalse(viewModel.uiState.value.pushEnabled)
        assertEquals("Light", viewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `support viewmodel selects topic`() {
        val viewModel = SupportViewModel()

        viewModel.onAction(SupportAction.SelectTopic("Radar"))

        assertEquals("Radar", viewModel.uiState.value.selectedTopic)
    }

    @Test
    fun `about viewmodel toggles build channel`() {
        val viewModel = AboutViewModel()

        assertEquals("Dev", viewModel.uiState.value.buildChannel)
        viewModel.onAction(AboutAction.ToggleBuildChannel)
        assertEquals("QA", viewModel.uiState.value.buildChannel)
    }

    @Test
    fun `search viewmodel selects category`() {
        val viewModel = SearchViewModel()

        viewModel.onAction(SearchAction.SelectCategory("Market"))

        assertEquals("Market", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `notifications viewmodel selects filter`() {
        val viewModel = NotificationsViewModel()

        viewModel.onAction(NotificationsAction.SelectFilter("All"))

        assertEquals("All", viewModel.uiState.value.selectedFilter)
    }
}
