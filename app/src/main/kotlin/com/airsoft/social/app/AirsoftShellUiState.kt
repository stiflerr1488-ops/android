package com.airsoft.social.app

import com.airsoft.social.core.model.AppTab
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.OnboardingState

data class AirsoftShellUiState(
    val authState: AuthState = AuthState.Unknown,
    val onboardingState: OnboardingState = OnboardingState.Required,
    val bootstrapRoute: String = "auth",
    val tabBadges: Map<AppTab, Int> = emptyMap(),
    val appSettings: AppSettings = AppSettings(),
)
