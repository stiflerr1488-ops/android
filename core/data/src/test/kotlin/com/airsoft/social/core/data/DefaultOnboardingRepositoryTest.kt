package com.airsoft.social.core.data

import com.airsoft.social.core.datastore.OnboardingLocalDataSource
import com.airsoft.social.core.model.OnboardingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeOnboardingLocalDataSource : OnboardingLocalDataSource {
    private val state = MutableStateFlow(false)

    override fun observeCompleted(): Flow<Boolean> = state

    override suspend fun setCompleted(completed: Boolean) {
        state.value = completed
    }
}

class DefaultOnboardingRepositoryTest {
    @Test
    fun `completeOnboarding updates state`() = runTest {
        val repository = DefaultOnboardingRepository(FakeOnboardingLocalDataSource())

        repository.completeOnboarding()

        assertEquals(OnboardingState.Completed, repository.onboardingState.first())
    }
}

