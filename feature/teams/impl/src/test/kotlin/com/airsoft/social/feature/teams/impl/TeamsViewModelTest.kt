package com.airsoft.social.feature.teams.impl

import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewTeamsRepository
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TeamsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `maps repository team roster and recruiting feed into ui state`() = runTest {
        val viewModel = TeamsViewModel(
            PreviewTeamsRepository(DemoSocialRepositoryProvider.repository),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.myTeamCard.title.contains("EASY WINNER"))
        assertTrue(viewModel.uiState.value.roster.isNotEmpty())
        assertTrue(viewModel.uiState.value.recruitingFeed.isNotEmpty())
    }

    @Test
    fun `select recruiting filter updates state`() = runTest {
        val viewModel = TeamsViewModel(
            PreviewTeamsRepository(DemoSocialRepositoryProvider.repository),
        )

        viewModel.onAction(TeamsAction.SelectRecruitingFilter("Medic"))
        advanceUntilIdle()

        assertEquals("Medic", viewModel.uiState.value.selectedRecruitingFilter)
    }
}
