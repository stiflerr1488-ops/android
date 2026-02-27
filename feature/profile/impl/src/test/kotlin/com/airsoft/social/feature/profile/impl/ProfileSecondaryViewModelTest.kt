package com.airsoft.social.feature.profile.impl

import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.data.ProfileRepository
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AchievementRow
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.GameHistoryRow
import com.airsoft.social.core.model.GearCategorySummary
import com.airsoft.social.core.model.PrivacySettings
import com.airsoft.social.core.model.TrustBadgeRow
import com.airsoft.social.core.model.User
import com.airsoft.social.core.model.UserSession
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class EditableFakeProfileRepository(
    user: User,
) : ProfileRepository {
    private val state = MutableStateFlow(user)

    override fun observeCurrentUser(): Flow<User?> = state
    override fun observeUser(userId: String): Flow<User?> = state
    override fun observeGearCategories(userId: String): Flow<List<GearCategorySummary>> = flowOf(emptyList())
    override fun observeGameHistory(userId: String): Flow<List<GameHistoryRow>> = flowOf(emptyList())
    override fun observeAchievements(userId: String): Flow<List<AchievementRow>> = flowOf(emptyList())
    override fun observeTrustBadges(userId: String): Flow<List<TrustBadgeRow>> = flowOf(emptyList())

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
    ): AppResult<Unit> {
        state.value = state.value.copy(
            callsign = callsign,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            region = region,
            exitRadiusKm = exitRadiusKm,
            avatarUrl = avatarUrl,
            bannerUrl = bannerUrl,
            privacySettings = privacySettings,
        )
        return AppResult.Success(Unit)
    }
}

private class FakeSessionRepository(
    initialState: AuthState,
) : SessionRepository {
    private val state = MutableStateFlow(initialState)
    override val authState: Flow<AuthState> = state

    override suspend fun signInWithEmail(email: String, password: String): AuthResult =
        AuthResult.Failure("not used")

    override suspend fun registerWithEmail(email: String, password: String, callsign: String?): AuthResult =
        AuthResult.Failure("not used")

    override suspend fun signInGuest(callsign: String?): AuthResult =
        AuthResult.Failure("not used")

    override suspend fun upgradeGuestToEmail(email: String, password: String, callsign: String?): AuthResult =
        AuthResult.Failure("not used")

    override suspend fun signOut() = Unit

    override suspend fun currentAuthState(): AuthState = state.value
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSecondaryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `edit profile viewmodel loads user and updates fields`() = runTest {
        val repository = EditableFakeProfileRepository(
            User(
                id = "self",
                callsign = "Nomad",
                firstName = "Nomad",
                lastName = "Fox",
                privacySettings = PrivacySettings(),
            ),
        )
        val sessionRepository = FakeSessionRepository(
            AuthState.SignedIn(
                UserSession(userId = "self", displayName = "Nomad", email = "n@example.com", isGuest = false),
            ),
        )
        val viewModel = EditProfileViewModel(repository, sessionRepository)

        viewModel.load("self")
        advanceUntilIdle()
        viewModel.onAction(EditProfileAction.CallsignChanged("CQB_Fox"))
        viewModel.onAction(EditProfileAction.ToggleShowPhone(true))
        viewModel.onAction(EditProfileAction.SaveChangesClicked)
        advanceUntilIdle()

        assertEquals("CQB_Fox", viewModel.uiState.value.callsign)
        assertTrue(viewModel.uiState.value.privacySettings.showPhone)
        assertEquals("CQB_Fox", repository.observeCurrentUser().first()?.callsign)
    }

    @Test
    fun `guest session cannot save profile`() = runTest {
        val repository = EditableFakeProfileRepository(
            User(
                id = "self",
                callsign = "Guest",
                firstName = "Guest",
                lastName = "",
                privacySettings = PrivacySettings(),
            ),
        )
        val sessionRepository = FakeSessionRepository(
            AuthState.SignedIn(
                UserSession(userId = "self", displayName = "Guest", isGuest = true),
            ),
        )
        val viewModel = EditProfileViewModel(repository, sessionRepository)

        viewModel.load("self")
        advanceUntilIdle()
        viewModel.onAction(EditProfileAction.SaveChangesClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage?.contains("Гостевой") == true)
    }
}
