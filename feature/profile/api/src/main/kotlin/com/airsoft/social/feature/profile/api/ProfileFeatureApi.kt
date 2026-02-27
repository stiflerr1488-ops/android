package com.airsoft.social.feature.profile.api

data class ProfileRouteContract(
    val route: String,
    val title: String,
    val subtitle: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object ProfileFeatureApi {
    const val ROUTE: String = "profile"

    const val USER_ID_ARG: String = "userId"
    const val ProfileEditRoutePattern: String = "profile/{$USER_ID_ARG}/edit"
    const val ProfileInventoryRoutePattern: String = "profile/{$USER_ID_ARG}/inventory"
    const val ProfileAchievementsRoutePattern: String = "profile/{$USER_ID_ARG}/achievements"
    const val ProfileTrustBadgesRoutePattern: String = "profile/{$USER_ID_ARG}/trust-badges"

    val contract = ProfileRouteContract(
        route = ROUTE,
        title = "Профиль",
        subtitle = "Шапка, команда, снаряжение, история игр, достижения и доверие",
        requiresAuth = true,
        requiresOnboarding = true,
    )

    fun profileEditRoute(userId: String): String = "profile/$userId/edit"
    fun profileInventoryRoute(userId: String): String = "profile/$userId/inventory"
    fun profileAchievementsRoute(userId: String): String = "profile/$userId/achievements"
    fun profileTrustBadgesRoute(userId: String): String = "profile/$userId/trust-badges"
}
