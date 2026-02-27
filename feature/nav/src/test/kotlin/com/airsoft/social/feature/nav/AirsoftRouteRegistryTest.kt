package com.airsoft.social.feature.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AirsoftRouteRegistryTest {
    @Test
    fun `route registry contains five top level destinations`() {
        assertEquals(5, AirsoftRouteRegistry.topLevelDestinations.size)
    }

    @Test
    fun `auth and onboarding routes are present`() {
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.authRoute))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.onboardingRoute))
    }

    @Test
    fun `profile edit route and tactical route are registered`() {
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(com.airsoft.social.feature.profile.api.ProfileFeatureApi.ProfileEditRoutePattern))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.TacticalReserved))
    }
}
