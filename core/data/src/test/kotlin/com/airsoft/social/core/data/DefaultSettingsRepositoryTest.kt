package com.airsoft.social.core.data

import com.airsoft.social.core.datastore.LocalSettingsDataSource
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AppThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultSettingsRepositoryTest {

    @Test
    fun `updates local settings values`() = runTest {
        val dataSource = FakeLocalSettingsDataSource()
        val repository = DefaultSettingsRepository(dataSource)

        repository.setThemePreference(AppThemePreference.Dark)
        repository.setPushEnabled(false)
        repository.setTelemetryEnabled(true)
        repository.setTacticalQuickLaunchEnabled(false)

        val state = repository.observeSettings().first()
        assertEquals(AppThemePreference.Dark, state.themePreference)
        assertEquals(false, state.pushEnabled)
        assertEquals(true, state.telemetryEnabled)
        assertEquals(false, state.tacticalQuickLaunchEnabled)
    }
}

private class FakeLocalSettingsDataSource : LocalSettingsDataSource {
    private val settings = MutableStateFlow(AppSettings())

    override fun observeSettings(): Flow<AppSettings> = settings.asStateFlow()

    override suspend fun setThemePreference(value: AppThemePreference) {
        settings.value = settings.value.copy(themePreference = value)
    }

    override suspend fun setPushEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(pushEnabled = enabled)
    }

    override suspend fun setTelemetryEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(telemetryEnabled = enabled)
    }

    override suspend fun setTacticalQuickLaunchEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(tacticalQuickLaunchEnabled = enabled)
    }
}
