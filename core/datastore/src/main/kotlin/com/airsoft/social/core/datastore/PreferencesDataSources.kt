package com.airsoft.social.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AppThemePreference
import com.airsoft.social.core.model.UserSummary
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.airsoftPrefs: DataStore<Preferences> by preferencesDataStore(name = "airsoft_shell")

private object PrefKeys {
    val lastUserId = stringPreferencesKey("last_user_id")
    val lastUserCallsign = stringPreferencesKey("last_user_callsign")
    val lastUserAvatar = stringPreferencesKey("last_user_avatar")
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    val useFirebaseAdapters = booleanPreferencesKey("use_firebase_adapters")
    val coreSocialOnly = booleanPreferencesKey("core_social_only")
    val realProfileChats = booleanPreferencesKey("real_profile_chats")
    val realSocialAll = booleanPreferencesKey("real_social_all")
    val themePreference = stringPreferencesKey("theme_preference")
    val pushEnabled = booleanPreferencesKey("push_enabled")
    val telemetryEnabled = booleanPreferencesKey("telemetry_enabled")
    val tacticalQuickLaunchEnabled = booleanPreferencesKey("tactical_quick_launch_enabled")
}

private fun DataStore<Preferences>.safeData(): Flow<Preferences> = data.catch { err ->
    if (err is IOException) emit(emptyPreferences()) else throw err
}

class PreferencesSessionLocalDataSource(
    context: Context,
) : SessionLocalDataSource {
    private val store = context.airsoftPrefs

    override fun observeLastKnownUser(): Flow<UserSummary?> = store.safeData().map { prefs ->
        val userId = prefs[PrefKeys.lastUserId] ?: return@map null
        val callsign = prefs[PrefKeys.lastUserCallsign] ?: return@map null
        UserSummary(
            id = userId,
            callsign = callsign,
            avatarUrl = prefs[PrefKeys.lastUserAvatar],
        )
    }

    override suspend fun setLastKnownUser(user: UserSummary?) {
        store.edit { prefs ->
            if (user == null) {
                prefs.remove(PrefKeys.lastUserId)
                prefs.remove(PrefKeys.lastUserCallsign)
                prefs.remove(PrefKeys.lastUserAvatar)
                return@edit
            }
            prefs[PrefKeys.lastUserId] = user.id
            prefs[PrefKeys.lastUserCallsign] = user.callsign
            val avatarUrl = user.avatarUrl
            if (avatarUrl != null) {
                prefs[PrefKeys.lastUserAvatar] = avatarUrl
            } else {
                prefs.remove(PrefKeys.lastUserAvatar)
            }
        }
    }
}

class PreferencesOnboardingLocalDataSource(
    context: Context,
) : OnboardingLocalDataSource {
    private val store = context.airsoftPrefs

    override fun observeCompleted(): Flow<Boolean> = store.safeData().map { prefs ->
        prefs[PrefKeys.onboardingCompleted] ?: false
    }

    override suspend fun setCompleted(completed: Boolean) {
        store.edit { prefs ->
            prefs[PrefKeys.onboardingCompleted] = completed
        }
    }
}

class PreferencesLocalFeatureFlagsDataSource(
    context: Context,
) : LocalFeatureFlagsDataSource {
    private val store = context.airsoftPrefs

    override fun observeFlags(): Flow<LocalFeatureFlags> = store.safeData().map { prefs ->
        LocalFeatureFlags(
            useFirebaseAdapters = prefs[PrefKeys.useFirebaseAdapters] ?: false,
            coreSocialOnly = prefs[PrefKeys.coreSocialOnly] ?: true,
            realProfileChats = prefs[PrefKeys.realProfileChats] ?: true,
            realSocialAll = prefs[PrefKeys.realSocialAll] ?: false,
        )
    }

    override suspend fun setUseFirebaseAdapters(enabled: Boolean) {
        store.edit { prefs ->
            prefs[PrefKeys.useFirebaseAdapters] = enabled
        }
    }

    override suspend fun setCoreSocialOnly(enabled: Boolean) {
        store.edit { prefs ->
            prefs[PrefKeys.coreSocialOnly] = enabled
        }
    }

    override suspend fun setRealProfileChats(enabled: Boolean) {
        store.edit { prefs ->
            prefs[PrefKeys.realProfileChats] = enabled
        }
    }

    override suspend fun setRealSocialAll(enabled: Boolean) {
        store.edit { prefs ->
            prefs[PrefKeys.realSocialAll] = enabled
        }
    }
}

class PreferencesLocalSettingsDataSource(
    context: Context,
) : LocalSettingsDataSource {
    private val store = context.airsoftPrefs

    override fun observeSettings(): Flow<AppSettings> = store.safeData().map { prefs ->
        AppSettings(
            themePreference = prefs[PrefKeys.themePreference]
                ?.let { raw -> AppThemePreference.entries.firstOrNull { it.name == raw } }
                ?: AppThemePreference.System,
            pushEnabled = prefs[PrefKeys.pushEnabled] ?: true,
            telemetryEnabled = prefs[PrefKeys.telemetryEnabled] ?: false,
            tacticalQuickLaunchEnabled = prefs[PrefKeys.tacticalQuickLaunchEnabled] ?: true,
        )
    }

    override suspend fun setThemePreference(value: AppThemePreference) {
        store.edit { prefs -> prefs[PrefKeys.themePreference] = value.name }
    }

    override suspend fun setPushEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[PrefKeys.pushEnabled] = enabled }
    }

    override suspend fun setTelemetryEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[PrefKeys.telemetryEnabled] = enabled }
    }

    override suspend fun setTacticalQuickLaunchEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[PrefKeys.tacticalQuickLaunchEnabled] = enabled }
    }
}
