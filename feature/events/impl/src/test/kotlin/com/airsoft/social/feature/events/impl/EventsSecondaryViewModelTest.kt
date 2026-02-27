package com.airsoft.social.feature.events.impl

import com.airsoft.social.core.data.DemoEditorDraftPreviewRepositoryProvider
import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewEventEditorRepository
import com.airsoft.social.core.data.PreviewEventsRepository
import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventsSecondaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `event detail viewmodel loads id and toggles registration`() = runTest {
        val viewModel = EventDetailViewModel(
            PreviewEventsRepository(DemoSocialRepositoryProvider.repository),
        )

        viewModel.load("night-raid-north")
        advanceUntilIdle()
        viewModel.onAction(EventDetailAction.SelectTab("Timeline"))
        viewModel.onAction(EventDetailAction.ToggleRegistrationOpen)

        assertEquals("night-raid-north", viewModel.uiState.value.eventId)
        assertEquals("Timeline", viewModel.uiState.value.selectedTab)
        assertFalse(viewModel.uiState.value.registrationOpen)
        assertTrue(viewModel.uiState.value.timelineRows.isNotEmpty())
    }

    @Test
    fun `event editor viewmodel loads context and toggles approval`() = runTest {
        val viewModel = EventEditorViewModel(
            PreviewEventEditorRepository(
                socialPreviewRepository = DemoSocialRepositoryProvider.repository,
                editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
            ),
        )

        viewModel.load(editorMode = EditorMode.Draft, editorRefId = "draft-event-1")
        viewModel.onAction(EventEditorAction.SelectStep("Rules"))
        viewModel.onAction(EventEditorAction.ToggleManualApproval)

        assertEquals(EditorMode.Draft, viewModel.uiState.value.editorMode)
        assertEquals("draft-event-1", viewModel.uiState.value.editorRefId)
        assertEquals("Rules", viewModel.uiState.value.selectedStep)
        assertTrue(viewModel.uiState.value.manualApprovalEnabled)
    }

    @Test
    fun `event editor loads seeded draft context`() = runTest {
        val viewModel = EventEditorViewModel(
            PreviewEventEditorRepository(
                socialPreviewRepository = DemoSocialRepositoryProvider.repository,
                editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
            ),
        )

        viewModel.load(editorMode = EditorMode.Create, editorRefId = "draft-event-night-raid")

        assertEquals("draft-event-night-raid", viewModel.uiState.value.editorRefId)
        assertTrue(viewModel.uiState.value.selectedStep.isNotBlank())
        assertTrue(viewModel.uiState.value.manualApprovalEnabled)
    }
}
