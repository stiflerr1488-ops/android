package com.airsoft.social.feature.settings.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.settings.api.SettingsFeatureApi

fun NavGraphBuilder.registerSettingsFeatureGraph(
    navigateToSettingsRoute: (String) -> Unit,
    onOpenSupport: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenAuth: (String?) -> Unit,
    onSignOut: () -> Unit,
) {
    composable(SettingsFeatureApi.SettingsRoute) {
        SettingsShellRoute(
            onOpenAccount = { navigateToSettingsRoute(SettingsFeatureApi.SettingsAccountRoute) },
            onOpenPrivacy = { navigateToSettingsRoute(SettingsFeatureApi.SettingsPrivacyRoute) },
            onOpenPermissions = { navigateToSettingsRoute(SettingsFeatureApi.SettingsPermissionsRoute) },
            onOpenSecurity = { navigateToSettingsRoute(SettingsFeatureApi.SettingsSecurityRoute) },
            onOpenBattery = { navigateToSettingsRoute(SettingsFeatureApi.SettingsBatteryRoute) },
            onOpenSupport = onOpenSupport,
            onOpenAbout = onOpenAbout,
        )
    }
    composable(
        route = SettingsFeatureApi.SettingsAccountRoutePattern,
        arguments = listOf(
            navArgument(SettingsFeatureApi.SettingsAccountReasonArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { entry ->
        SettingsAccountShellRoute(
            accessDeniedReasonArg = entry.arguments?.getString(SettingsFeatureApi.SettingsAccountReasonArg),
            onOpenAuth = onOpenAuth,
            onOpenSupport = onOpenSupport,
            onOpenAdminDashboard = onOpenAdminDashboard,
            onSignOut = onSignOut,
        )
    }
    composable(SettingsFeatureApi.SettingsPrivacyRoute) {
        SettingsPrivacyShellRoute(
            onOpenRules = onOpenRules,
            onOpenSupport = onOpenSupport,
        )
    }
    composable(SettingsFeatureApi.SettingsPermissionsRoute) {
        SettingsPermissionsShellRoute()
    }
    composable(SettingsFeatureApi.SettingsSecurityRoute) {
        SettingsSecurityShellRoute()
    }
    composable(SettingsFeatureApi.SettingsBatteryRoute) {
        SettingsBatteryShellRoute()
    }
}
