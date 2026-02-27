package com.airsoft.social.feature.profile.impl

import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewProfileRepository
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `maps current user into ui state`() = runTest {
        val viewModel = ProfileViewModel(
            PreviewProfileRepository(DemoSocialRepositoryProvider.repository),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profileHeader.title.isNotBlank())
        assertTrue(viewModel.uiState.value.teamInfo != null)
        assertTrue(viewModel.uiState.value.gameHistory.isNotEmpty())
    }

    @Test
    fun `profile actions keep simplified state`() = runTest {
        val viewModel = ProfileViewModel(
            PreviewProfileRepository(DemoSocialRepositoryProvider.repository),
        )

        val initialHistory = viewModel.uiState.value.gameHistory
        viewModel.onAction(ProfileAction.OpenEditProfileDemoClicked)
        viewModel.onAction(ProfileAction.SignOutClicked)

        assertEquals(initialHistory, viewModel.uiState.value.gameHistory)
    }
}
