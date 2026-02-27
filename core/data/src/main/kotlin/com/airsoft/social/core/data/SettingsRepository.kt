package com.airsoft.social.core.data

import com.airsoft.social.core.datastore.LocalSettingsDataSource
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AppThemePreference
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setThemePreference(value: AppThemePreference)
    suspend fun setPushEnabled(enabled: Boolean)
    suspend fun setTelemetryEnabled(enabled: Boolean)
    suspend fun setTacticalQuickLaunchEnabled(enabled: Boolean)
}

class DefaultSettingsRepository(
    private val localDataSource: LocalSettingsDataSource,
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = localDataSource.observeSettings()

    override suspend fun setThemePreference(value: AppThemePreference) {
        localDataSource.setThemePreference(value)
    }

    override suspend fun setPushEnabled(enabled: Boolean) {
        localDataSource.setPushEnabled(enabled)
    }

    override suspend fun setTelemetryEnabled(enabled: Boolean) {
        localDataSource.setTelemetryEnabled(enabled)
    }

    override suspend fun setTacticalQuickLaunchEnabled(enabled: Boolean) {
        localDataSource.setTacticalQuickLaunchEnabled(enabled)
    }
}
