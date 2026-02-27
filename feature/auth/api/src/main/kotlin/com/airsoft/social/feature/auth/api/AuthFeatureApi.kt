package com.airsoft.social.feature.auth.api

data class AuthRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object AuthFeatureApi {
    const val ROUTE: String = "auth"
    const val REASON_ARG: String = "reason"
    const val ROUTE_PATTERN: String = "$ROUTE?$REASON_ARG={$REASON_ARG}"

    const val REASON_REGISTRATION_REQUIRED: String = "registration_required"

    fun authRoute(reason: String? = null): String = when {
        reason.isNullOrBlank() -> ROUTE
        else -> "$ROUTE?$REASON_ARG=$reason"
    }

    val contract = AuthRouteContract(
        route = ROUTE,
        title = "Auth",
        requiresAuth = false,
        requiresOnboarding = false,
    )
}
