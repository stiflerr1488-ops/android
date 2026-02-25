package com.airsoft.social.core.model

sealed interface OnboardingState {
    data object Required : OnboardingState
    data object Completed : OnboardingState
}

