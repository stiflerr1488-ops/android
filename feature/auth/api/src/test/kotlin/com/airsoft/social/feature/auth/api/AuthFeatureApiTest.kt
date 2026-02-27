package com.airsoft.social.feature.auth.api

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthFeatureApiTest {

    @Test
    fun `auth route builder appends reason only when provided`() {
        assertEquals(AuthFeatureApi.ROUTE, AuthFeatureApi.authRoute())
        assertEquals(
            "auth?reason=registration_required",
            AuthFeatureApi.authRoute(AuthFeatureApi.REASON_REGISTRATION_REQUIRED),
        )
    }

    @Test
    fun `auth route pattern keeps optional reason arg`() {
        assertEquals("auth?reason={reason}", AuthFeatureApi.ROUTE_PATTERN)
    }
}
