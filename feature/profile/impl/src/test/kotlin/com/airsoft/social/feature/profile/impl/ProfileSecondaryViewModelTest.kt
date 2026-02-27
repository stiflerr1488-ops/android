package com.airsoft.social.feature.profile.impl

import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewProfileRepository
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSecondaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `edit profile viewmodel loads user and updates fields`() = runTest {
        val repository = PreviewProfileRepository(DemoSocialRepositoryProvider.repository)
        val viewModel = EditProfileViewModel(repository)

        viewModel.load("self")
        advanceUntilIdle()
        val initialShowPhone = viewModel.uiState.value.privacySettings.showPhone

        viewModel.onAction(EditProfileAction.CallsignChanged("CQB_Fox"))
        viewModel.onAction(EditProfileAction.ToggleShowPhone(!initialShowPhone))
        viewModel.onAction(
            EditProfileAction.MessageVisibilityChanged(MessageVisibilityMode.TEAM_ONLY),
        )

        assertEquals("self", viewModel.uiState.value.userId)
        assertEquals("CQB_Fox", viewModel.uiState.value.callsign)
        assertEquals(!initialShowPhone, viewModel.uiState.value.privacySettings.showPhone)
        assertFalse(viewModel.uiState.value.privacySettings.allowDirectMessages)
        assertTrue(viewModel.uiState.value.privacySettings.allowTeamInvites)
    }

    @Test
    fun `profile inventory viewmodel loads user and toggles sell link`() = runTest {
        val repository = PreviewProfileRepository(DemoSocialRepositoryProvider.repository)
        val viewModel = ProfileInventoryViewModel(repository)

        viewModel.load("user-1")
        advanceUntilIdle()
        viewModel.onAction(ProfileInventoryAction.SelectLoadout("Вторичный"))
        viewModel.onAction(ProfileInventoryAction.ToggleSellFromInventory)

        assertEquals("user-1", viewModel.uiState.value.userId)
        assertEquals("Вторичный", viewModel.uiState.value.selectedLoadout)
        assertFalse(viewModel.uiState.value.sellFromInventoryEnabled)
    }

    @Test
    fun `profile achievements viewmodel loads user and cycles category`() = runTest {
        val repository = PreviewProfileRepository(DemoSocialRepositoryProvider.repository)
        val viewModel = ProfileAchievementsViewModel(repository)

        viewModel.load("user-1")
        advanceUntilIdle()
        val initialCategory = viewModel.uiState.value.selectedCategory
        viewModel.onAction(ProfileAchievementsAction.CycleCategory)

        assertEquals("user-1", viewModel.uiState.value.userId)
        assertTrue(viewModel.uiState.value.achievementRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedCategory != initialCategory)
    }

    @Test
    fun `profile trust badges viewmodel loads user and cycles scope`() = runTest {
        val repository = PreviewProfileRepository(DemoSocialRepositoryProvider.repository)
        val viewModel = ProfileTrustBadgesViewModel(repository)

        viewModel.load("user-1")
        advanceUntilIdle()
        val initialScope = viewModel.uiState.value.selectedScope
        viewModel.onAction(ProfileTrustBadgesAction.CycleScope)

        assertEquals("user-1", viewModel.uiState.value.userId)
        assertTrue(viewModel.uiState.value.badgeRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedScope != initialScope)
    }
}
