package com.airsoft.social.feature.teams.impl

import com.airsoft.social.core.data.DemoEditorDraftPreviewRepositoryProvider
import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewTeamEditorRepository
import com.airsoft.social.core.data.PreviewTeamsRepository
import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TeamsSecondaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `team detail viewmodel loads id and toggles state`() = runTest {
        val viewModel = TeamDetailViewModel(
            PreviewTeamsRepository(DemoSocialRepositoryProvider.repository),
        )

        viewModel.load("ew-easy-winner")
        advanceUntilIdle()
        viewModel.onAction(TeamDetailAction.SelectTab("Состав"))
        viewModel.onAction(TeamDetailAction.ToggleRecruitingOpen)

        assertEquals("ew-easy-winner", viewModel.uiState.value.teamId)
        assertEquals("Состав", viewModel.uiState.value.selectedTab)
        assertFalse(viewModel.uiState.value.recruitingOpen)
        assertTrue(viewModel.uiState.value.rosterSummary.isNotEmpty())
    }

    @Test
    fun `team editor viewmodel loads editor context and toggles auto approve`() = runTest {
        val viewModel = TeamEditorViewModel(
            PreviewTeamEditorRepository(
                socialPreviewRepository = DemoSocialRepositoryProvider.repository,
                editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
            ),
        )

        viewModel.load(editorMode = EditorMode.Edit, editorRefId = "team-42")
        viewModel.onAction(TeamEditorAction.SelectStep("Роли"))
        viewModel.onAction(TeamEditorAction.ToggleAutoApprove)

        assertEquals(EditorMode.Edit, viewModel.uiState.value.editorMode)
        assertEquals("team-42", viewModel.uiState.value.editorRefId)
        assertEquals("Роли", viewModel.uiState.value.selectedStep)
        assertFalse(viewModel.uiState.value.autoApproveEnabled)
    }

    @Test
    fun `team editor create mode starts with auto approve disabled`() = runTest {
        val viewModel = TeamEditorViewModel(
            PreviewTeamEditorRepository(
                socialPreviewRepository = DemoSocialRepositoryProvider.repository,
                editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
            ),
        )

        viewModel.load(editorMode = EditorMode.Create, editorRefId = "draft-1")

        assertFalse(viewModel.uiState.value.autoApproveEnabled)
    }

    @Test
    fun `team editor loads seeded draft context`() = runTest {
        val viewModel = TeamEditorViewModel(
            PreviewTeamEditorRepository(
                socialPreviewRepository = DemoSocialRepositoryProvider.repository,
                editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
            ),
        )

        viewModel.load(editorMode = EditorMode.Create, editorRefId = "draft-team-ew-001")

        assertEquals("draft-team-ew-001", viewModel.uiState.value.editorRefId)
        assertEquals("Роли", viewModel.uiState.value.selectedStep)
        assertFalse(viewModel.uiState.value.autoApproveEnabled)
    }
}
