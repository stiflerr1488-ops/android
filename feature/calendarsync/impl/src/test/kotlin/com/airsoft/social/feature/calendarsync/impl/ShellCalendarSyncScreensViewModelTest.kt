package com.airsoft.social.feature.calendarsync.impl

import org.junit.Assert.assertTrue
import org.junit.Test

class ShellCalendarSyncScreensViewModelTest {
    @Test
    fun `calendar sync viewmodel cycles target and toggles sync`() {
        val viewModel = CalendarSyncViewModel()

        val initialTarget = viewModel.uiState.value.selectedTarget
        val initialEnabled = viewModel.uiState.value.syncEnabled
        viewModel.onAction(CalendarSyncAction.CycleTarget)
        viewModel.onAction(CalendarSyncAction.ToggleSync)

        assertTrue(viewModel.uiState.value.selectedTarget != initialTarget)
        assertTrue(viewModel.uiState.value.syncEnabled != initialEnabled)
        assertTrue(viewModel.uiState.value.exportRows.isNotEmpty())
    }

    @Test
    fun `calendar sync reminders viewmodel cycles preset`() {
        val viewModel = CalendarSyncRemindersViewModel()

        val initial = viewModel.uiState.value.selectedPreset
        viewModel.onAction(CalendarSyncRemindersAction.CyclePreset)

        assertTrue(viewModel.uiState.value.selectedPreset != initial)
        assertTrue(viewModel.uiState.value.reminderRows.isNotEmpty())
    }

    @Test
    fun `calendar sync export history viewmodel cycles scope`() {
        val viewModel = CalendarSyncExportHistoryViewModel()

        val initial = viewModel.uiState.value.selectedScope
        viewModel.onAction(CalendarSyncExportHistoryAction.CycleScope)

        assertTrue(viewModel.uiState.value.selectedScope != initial)
        assertTrue(viewModel.uiState.value.historyRows.isNotEmpty())
    }
}
