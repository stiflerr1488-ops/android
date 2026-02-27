package com.airsoft.social.feature.settings.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsFeatureApiTest {

    @Test
    fun `all settings routes are exposed for host registries`() {
        val expected = setOf(
            SettingsFeatureApi.SettingsRoute,
            SettingsFeatureApi.SettingsAccountRoute,
            SettingsFeatureApi.SettingsAccountRoutePattern,
            SettingsFeatureApi.SettingsPrivacyRoute,
            SettingsFeatureApi.SettingsPermissionsRoute,
            SettingsFeatureApi.SettingsSecurityRoute,
            SettingsFeatureApi.SettingsBatteryRoute,
        )

        assertEquals(expected, SettingsFeatureApi.allRoutes)
        assertTrue(SettingsFeatureApi.allRoutes.size == expected.size)
    }

    @Test
    fun `settings account route builder appends reason only when provided`() {
        assertEquals(
            SettingsFeatureApi.SettingsAccountRoute,
            SettingsFeatureApi.settingsAccountRoute(),
        )
        assertEquals(
            "settings/account?reason=commercial_role_required",
            SettingsFeatureApi.settingsAccountRoute(
                SettingsFeatureApi.AccountReasonCommercialRoleRequired,
            ),
        )
    }
}
