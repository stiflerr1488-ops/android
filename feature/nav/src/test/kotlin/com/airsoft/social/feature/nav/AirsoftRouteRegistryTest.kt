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
    fun `secondary skeleton routes are registered`() {
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.TeamDetailDemo))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.EventDetailDemo))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.MarketplaceListingDetailDemo))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.ProfileEditDemo))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.ChatRoomDemo))
    }

    @Test
    fun `shell utility routes are registered`() {
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.Search))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.Notifications))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.Settings))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.Support))
        assertTrue(AirsoftRouteRegistry.allRoutes.contains(AirsoftRoutes.About))
    }
}
