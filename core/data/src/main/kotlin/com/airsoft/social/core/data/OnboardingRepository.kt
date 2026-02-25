package com.airsoft.social.core.data

import com.airsoft.social.core.datastore.OnboardingLocalDataSource
import com.airsoft.social.core.model.OnboardingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface OnboardingRepository {
    val onboardingState: Flow<OnboardingState>
    suspend fun completeOnboarding()
    suspend fun resetOnboarding()
}

class DefaultOnboardingRepository(
    private val localDataSource: OnboardingLocalDataSource,
) : OnboardingRepository {
    override val onboardingState: Flow<OnboardingState> = localDataSource.observeCompleted().map { completed ->
        if (completed) OnboardingState.Completed else OnboardingState.Required
    }

    override suspend fun completeOnboarding() {
        localDataSource.setCompleted(true)
    }

    override suspend fun resetOnboarding() {
        localDataSource.setCompleted(false)
    }
}

