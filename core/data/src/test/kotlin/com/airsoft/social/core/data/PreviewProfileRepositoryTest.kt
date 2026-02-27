package com.airsoft.social.core.data

import com.airsoft.social.core.datastore.SessionLocalDataSource
import com.airsoft.social.core.model.UserSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeProfileSessionLocalDataSource(
    initial: UserSummary? = null,
) : SessionLocalDataSource {
    private val state = MutableStateFlow(initial)

    override fun observeLastKnownUser(): Flow<UserSummary?> = state

    override suspend fun setLastKnownUser(user: UserSummary?) {
        state.value = user
    }
}

class PreviewProfileRepositoryTest {

    private val repository = PreviewProfileRepository(DemoSocialRepositoryProvider.repository)

    @Test
    fun `observe current user returns self`() = runTest {
        val user = repository.observeCurrentUser().first()

        assertNotNull(user)
        assertEquals("self", user?.id)
    }

    @Test
    fun `observe user returns requested user`() = runTest {
        val user = repository.observeUser("self").first()

        assertNotNull(user)
        assertEquals("self", user?.id)
        assertTrue(user?.callsign?.isNotBlank() == true)
    }

    @Test
    fun `update user profile persists new callsign`() = runTest {
        val updated = repository.updateUserProfile(
            userId = "self",
            callsign = "Raptor",
            firstName = "Artem",
            lastName = "Volkov",
            bio = "updated",
            region = "SPB",
            exitRadiusKm = 80,
            avatarUrl = "https://example.com/a.jpg",
            bannerUrl = "https://example.com/b.jpg",
            privacySettings = com.airsoft.social.core.model.PrivacySettings(),
        )

        val user = repository.observeUser("self").first()
        assertTrue(updated)
        assertEquals("Raptor", user?.callsign)
        assertEquals("SPB", user?.region)
        assertEquals(80, user?.exitRadiusKm)
    }

    @Test
    fun `observe current user resolves session identity instead of demo self`() = runTest {
        val local = FakeProfileSessionLocalDataSource(
            UserSummary(
                id = "email:fox@airsoft.social",
                callsign = "CQB_Fox",
            ),
        )
        val repository = PreviewProfileRepository(
            previewRepository = DemoSocialRepositoryProvider.repository,
            sessionLocalDataSource = local,
        )

        val user = repository.observeCurrentUser().first()

        assertNotNull(user)
        assertEquals("email:fox@airsoft.social", user?.id)
        assertEquals("CQB_Fox", user?.callsign)
        assertNull(user?.teamName)
    }

    @Test
    fun `update self profile updates session user and local summary`() = runTest {
        val local = FakeProfileSessionLocalDataSource(
            UserSummary(
                id = "email:fox@airsoft.social",
                callsign = "CQB_Fox",
            ),
        )
        val repository = PreviewProfileRepository(
            previewRepository = DemoSocialRepositoryProvider.repository,
            sessionLocalDataSource = local,
        )

        val updated = repository.updateUserProfile(
            userId = "self",
            callsign = "Raptor",
            firstName = "Roman",
            lastName = "Fox",
            bio = "updated",
            region = "SPB",
            exitRadiusKm = 80,
            avatarUrl = "https://example.com/a.jpg",
            bannerUrl = "https://example.com/b.jpg",
            privacySettings = com.airsoft.social.core.model.PrivacySettings(),
        )

        val selfUser = repository.observeUser("self").first()
        val directUser = repository.observeUser("email:fox@airsoft.social").first()
        val summary = local.observeLastKnownUser().first()

        assertTrue(updated)
        assertEquals("email:fox@airsoft.social", selfUser?.id)
        assertEquals("Raptor", selfUser?.callsign)
        assertEquals("Raptor", directUser?.callsign)
        assertEquals("Raptor", summary?.callsign)
        assertEquals("https://example.com/a.jpg", summary?.avatarUrl)
    }
}
