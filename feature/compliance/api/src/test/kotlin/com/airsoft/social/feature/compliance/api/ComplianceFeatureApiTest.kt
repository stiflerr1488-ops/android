package com.airsoft.social.feature.compliance.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplianceFeatureApiTest {

    @Test
    fun `all compliance routes are exposed for host registries`() {
        val expected = setOf(
            ComplianceFeatureApi.RulesRoute,
            ComplianceFeatureApi.ComplianceReportBlockHideRoute,
            ComplianceFeatureApi.ComplianceAdvertisingLabelsRoute,
            ComplianceFeatureApi.ComplianceAgeGateRoute,
            ComplianceFeatureApi.CompliancePrivacyDefaultsRoute,
            ComplianceFeatureApi.ComplianceGdprRightsRoute,
        )

        assertEquals(expected, ComplianceFeatureApi.allRoutes)
        assertTrue(ComplianceFeatureApi.allRoutes.size == expected.size)
    }
}
