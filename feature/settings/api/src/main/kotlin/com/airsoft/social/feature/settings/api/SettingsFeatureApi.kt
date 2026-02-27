package com.airsoft.social.feature.settings.api

object SettingsFeatureApi {
    const val SettingsRoute = "settings"
    const val SettingsAccountRoute = "settings/account"
    const val SettingsAccountReasonArg = "reason"
    const val SettingsAccountRoutePattern =
        "$SettingsAccountRoute?$SettingsAccountReasonArg={$SettingsAccountReasonArg}"
    const val AccountReasonCommercialRoleRequired = "commercial_role_required"
    const val AccountReasonModeratorRoleRequired = "moderator_role_required"
    const val AccountReasonAdminRoleRequired = "admin_role_required"
    const val SettingsPrivacyRoute = "settings/privacy"
    const val SettingsPermissionsRoute = "settings/permissions"
    const val SettingsSecurityRoute = "settings/security"
    const val SettingsBatteryRoute = "settings/battery"

    fun settingsAccountRoute(reason: String? = null): String = when {
        reason.isNullOrBlank() -> SettingsAccountRoute
        else -> "$SettingsAccountRoute?$SettingsAccountReasonArg=$reason"
    }

    val allRoutes: Set<String> = setOf(
        SettingsRoute,
        SettingsAccountRoute,
        SettingsAccountRoutePattern,
        SettingsPrivacyRoute,
        SettingsPermissionsRoute,
        SettingsSecurityRoute,
        SettingsBatteryRoute,
    )
}
