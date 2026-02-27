package com.airsoft.social.core.datastore

import com.airsoft.social.core.model.UserSummary
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AppThemePreference

data class LocalFeatureFlags(
    val useFirebaseAdapters: Boolean = false,
    val coreSocialOnly: Boolean = true,
    val realProfileChats: Boolean = true,
    val realSocialAll: Boolean = false,
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
    suspend fun setCoreSocialOnly(enabled: Boolean)
    suspend fun setRealProfileChats(enabled: Boolean)
    suspend fun setRealSocialAll(enabled: Boolean)
}

interface LocalSettingsDataSource {
    fun observeSettings(): kotlinx.coroutines.flow.Flow<AppSettings>
    suspend fun setThemePreference(value: AppThemePreference)
    suspend fun setPushEnabled(enabled: Boolean)
    suspend fun setTelemetryEnabled(enabled: Boolean)
    suspend fun setTacticalQuickLaunchEnabled(enabled: Boolean)
}

