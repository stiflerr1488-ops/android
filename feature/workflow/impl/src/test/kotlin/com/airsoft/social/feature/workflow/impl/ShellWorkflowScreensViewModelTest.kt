package com.airsoft.social.feature.workflow.impl

import org.junit.Assert.assertTrue
import org.junit.Test

class ShellWorkflowScreensViewModelTest {
    @Test
    fun `saved filters viewmodel cycles domain and toggles auto apply`() {
        val viewModel = SavedFiltersViewModel()

        val initialDomain = viewModel.uiState.value.selectedDomain
        val initialAutoApply = viewModel.uiState.value.autoApplyEnabled
        viewModel.onAction(SavedFiltersAction.CycleDomain)
        viewModel.onAction(SavedFiltersAction.ToggleAutoApply)

        assertTrue(viewModel.uiState.value.selectedDomain != initialDomain)
        assertTrue(viewModel.uiState.value.autoApplyEnabled != initialAutoApply)
        assertTrue(viewModel.uiState.value.presetRows.isNotEmpty())
    }

    @Test
    fun `saved filter detail viewmodel loads once and cycles section`() {
        val viewModel = SavedFilterDetailViewModel()
        viewModel.load("filter-1")
        val initialSection = viewModel.uiState.value.selectedSection
        val initialSummarySize = viewModel.uiState.value.summaryRows.size

        viewModel.load("filter-1")
        viewModel.onAction(SavedFilterDetailAction.CycleSection)

        assertTrue(viewModel.uiState.value.selectedSection != initialSection)
        assertTrue(viewModel.uiState.value.summaryRows.size == initialSummarySize)
    }

    @Test
    fun `drafts viewmodel cycles type and toggles unsynced`() {
        val viewModel = DraftsViewModel()

        val initialType = viewModel.uiState.value.selectedType
        val initialUnsynced = viewModel.uiState.value.unsyncedOnly
        viewModel.onAction(DraftsAction.CycleType)
        viewModel.onAction(DraftsAction.ToggleUnsyncedOnly)

        assertTrue(viewModel.uiState.value.selectedType != initialType)
        assertTrue(viewModel.uiState.value.unsyncedOnly != initialUnsynced)
        assertTrue(viewModel.uiState.value.draftRows.isNotEmpty())
    }

    @Test
    fun `draft detail viewmodel loads and cycles tab`() {
        val viewModel = DraftDetailViewModel()
        viewModel.load("draft-1")
        val initialTab = viewModel.uiState.value.selectedTab

        viewModel.onAction(DraftDetailAction.CycleTab)

        assertTrue(viewModel.uiState.value.selectedTab != initialTab)
        assertTrue(viewModel.uiState.value.contentRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.publishRows.isNotEmpty())
    }
}
