package com.airsoft.social.core.datastore

import com.airsoft.social.core.model.UserSummary

data class LocalFeatureFlags(
    val useFirebaseAdapters: Boolean = false,
)

interface SessionLocalDataSource {
    fun observeLastKnownUser(): kotlinx.coroutines.flow.Flow<UserSummary?>
    suspend fun setLastKnownUser(user: UserSummary?)
}

interface OnboardingLocalDataSource {
    fun observeCompleted(): kotlinx.coroutines.flow.Flow<Boolean>
    suspend fun setCompleted(completed: Boolean)
}

interface LocalFeatureFlagsDataSource {
    fun observeFlags(): kotlinx.coroutines.flow.Flow<LocalFeatureFlags>
    suspend fun setUseFirebaseAdapters(enabled: Boolean)
}

