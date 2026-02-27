package com.airsoft.social.feature.compliance.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.airsoft.social.feature.compliance.api.ComplianceFeatureApi

fun NavGraphBuilder.registerComplianceFeatureGraph(
    navigateToComplianceRoute: (String) -> Unit,
) {
    composable(ComplianceFeatureApi.RulesRoute) {
        RulesComplianceShellRoute(
            onOpenReportBlockHide = {
                navigateToComplianceRoute(ComplianceFeatureApi.ComplianceReportBlockHideRoute)
            },
            onOpenAdvertisingLabels = {
                navigateToComplianceRoute(ComplianceFeatureApi.ComplianceAdvertisingLabelsRoute)
            },
            onOpenAgeGate = {
                navigateToComplianceRoute(ComplianceFeatureApi.ComplianceAgeGateRoute)
            },
            onOpenPrivacyDefaults = {
                navigateToComplianceRoute(ComplianceFeatureApi.CompliancePrivacyDefaultsRoute)
            },
            onOpenGdprRights = {
                navigateToComplianceRoute(ComplianceFeatureApi.ComplianceGdprRightsRoute)
            },
        )
    }
    composable(ComplianceFeatureApi.ComplianceReportBlockHideRoute) {
        ComplianceReportBlockHideShellRoute()
    }
    composable(ComplianceFeatureApi.ComplianceAdvertisingLabelsRoute) {
        ComplianceAdvertisingLabelsShellRoute()
    }
    composable(ComplianceFeatureApi.ComplianceAgeGateRoute) {
        ComplianceAgeGateShellRoute()
    }
    composable(ComplianceFeatureApi.CompliancePrivacyDefaultsRoute) {
        CompliancePrivacyDefaultsShellRoute()
    }
    composable(ComplianceFeatureApi.ComplianceGdprRightsRoute) {
        ComplianceGdprRightsShellRoute()
    }
}
