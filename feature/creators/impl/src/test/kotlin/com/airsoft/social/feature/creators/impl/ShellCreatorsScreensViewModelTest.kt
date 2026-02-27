package com.airsoft.social.feature.creators.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellCreatorsScreensViewModelTest {
    @Test
    fun `creators viewmodel cycles segment`() {
        val viewModel = CreatorsViewModel()

        val initial = viewModel.uiState.value.selectedSegment
        viewModel.onAction(CreatorsAction.CycleSegment)

        assertTrue(viewModel.uiState.value.selectedSegment != initial)
        assertTrue(viewModel.uiState.value.featuredRows.isNotEmpty())
    }

    @Test
    fun `creator detail viewmodel loads and cycles section`() {
        val viewModel = CreatorDetailViewModel()

        viewModel.load("teiwaz")
        val initial = viewModel.uiState.value.selectedSection
        viewModel.onAction(CreatorDetailAction.CycleSection)

        assertEquals("teiwaz", viewModel.uiState.value.creatorId)
        assertTrue(viewModel.uiState.value.profileRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedSection != initial)
    }

    @Test
    fun `creator studio viewmodel loads`() {
        val viewModel = CreatorStudioViewModel()

        viewModel.load("teiwaz")

        assertEquals("teiwaz", viewModel.uiState.value.creatorId)
        assertTrue(viewModel.uiState.value.studioRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.packageRows.isNotEmpty())
    }
}
