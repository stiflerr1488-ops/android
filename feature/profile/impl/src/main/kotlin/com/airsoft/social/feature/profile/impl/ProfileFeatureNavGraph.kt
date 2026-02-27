package com.airsoft.social.feature.profile.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.profile.api.ProfileFeatureApi

private const val DEFAULT_USER_ID = "self"

fun NavGraphBuilder.registerProfileFeatureGraph(
    onSignOut: () -> Unit,
    navigateToEditProfile: (String) -> Unit,
) {
    composable(ProfileFeatureApi.ROUTE) {
        ProfilePlaceholderScreen(
            onOpenEditProfile = { userId ->
                val resolvedUserId = userId.ifBlank { DEFAULT_USER_ID }
                navigateToEditProfile(ProfileFeatureApi.profileEditRoute(resolvedUserId))
            },
            onOpenPrivacySettings = { userId ->
                val resolvedUserId = userId.ifBlank { DEFAULT_USER_ID }
                navigateToEditProfile(ProfileFeatureApi.profileEditRoute(resolvedUserId))
            },
            onSignOut = onSignOut,
        )
    }
    composable(
        route = ProfileFeatureApi.ProfileEditRoutePattern,
        arguments = listOf(
            navArgument(ProfileFeatureApi.USER_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val userId = backStackEntry.arguments
            ?.getString(ProfileFeatureApi.USER_ID_ARG)
            .orEmpty()
            .ifBlank { DEFAULT_USER_ID }
        EditProfileRoute(
            userId = userId,
            onSaved = {},
            onCancel = {},
        )
    }
}
