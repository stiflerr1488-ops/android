package com.airsoft.social.feature.support.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportFeatureApiTest {

    @Test
    fun `all support routes are exposed for host registries`() {
        val expected = setOf(
            SupportFeatureApi.SupportRoute,
            SupportFeatureApi.SupportTicketsRoute,
            SupportFeatureApi.SupportChatRoute,
            SupportFeatureApi.SupportFaqRoute,
            SupportFeatureApi.SupportTicketDetailRoutePattern,
            SupportFeatureApi.AboutRoute,
        )

        assertEquals(expected, SupportFeatureApi.allRoutes)
        assertTrue(SupportFeatureApi.allRoutes.size == expected.size)
    }
}
