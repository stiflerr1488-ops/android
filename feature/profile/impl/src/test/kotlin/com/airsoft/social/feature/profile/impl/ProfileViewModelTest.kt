package com.airsoft.social.feature.profile.impl

import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.data.ProfileRepository
import com.airsoft.social.core.model.AchievementRow
import com.airsoft.social.core.model.GameHistoryRow
import com.airsoft.social.core.model.GearCategory
import com.airsoft.social.core.model.GearCategorySummary
import com.airsoft.social.core.model.PrivacySettings
import com.airsoft.social.core.model.TrustBadgeRow
import com.airsoft.social.core.model.User
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeProfileRepository(
    private val user: User?,
) : ProfileRepository {
    override fun observeCurrentUser(): Flow<User?> = flowOf(user)
    override fun observeUser(userId: String): Flow<User?> = flowOf(user)
    override fun observeGearCategories(userId: String): Flow<List<GearCategorySummary>> = flowOf(
        listOf(
            GearCategorySummary(
                category = GearCategory.PRIMARY_WEAPONS,
                displayName = "Приводы",
                icon = "primary_weapons",
                count = 1,
            ),
        ),
    )

    override fun observeGameHistory(userId: String): Flow<List<GameHistoryRow>> = flowOf(
        listOf(GameHistoryRow(id = "history-1", date = 1_770_000_000_000L, eventName = "Night Raid")),
    )

    override fun observeAchievements(userId: String): Flow<List<AchievementRow>> = flowOf(
        listOf(AchievementRow("a1", "100 игр", "Описание")),
    )

    override fun observeTrustBadges(userId: String): Flow<List<TrustBadgeRow>> = flowOf(
        listOf(TrustBadgeRow("t1", "Проверенный профиль", "Описание")),
    )

    override suspend fun updateUserProfile(
        userId: String,
        callsign: String,
        firstName: String,
        lastName: String,
        bio: String?,
        region: String?,
        exitRadiusKm: Int?,
        avatarUrl: String?,
        bannerUrl: String?,
        privacySettings: PrivacySettings,
    ): AppResult<Unit> = AppResult.Success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `maps current user into ui state`() = runTest {
        val viewModel = ProfileViewModel(
            FakeProfileRepository(
                user = User(
                    id = "user-1",
                    callsign = "Nomad",
                    firstName = "Nomad",
                    lastName = "",
                    teamName = "[EW]",
                    region = "Moscow",
                    isOnline = true,
                ),
            ),
        )

        advanceUntilIdle()
        val state = viewModel.uiState.first { it.profileHeader != null }

        assertEquals("Nomad", state.profileHeader?.title)
        assertTrue(state.gameHistory.isNotEmpty())
        assertTrue(state.achievements.isNotEmpty())
    }

    @Test
    fun `emits empty state when user is missing`() = runTest {
        val viewModel = ProfileViewModel(
            FakeProfileRepository(user = null),
        )

        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.profileHeader)
        assertTrue(viewModel.uiState.value.gameHistory.isEmpty())
    }
}
