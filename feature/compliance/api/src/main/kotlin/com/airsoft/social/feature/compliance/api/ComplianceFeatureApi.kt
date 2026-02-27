package com.airsoft.social.feature.compliance.api

object ComplianceFeatureApi {
    const val RulesRoute = "rules"
    const val ComplianceReportBlockHideRoute = "rules/report-block-hide"
    const val ComplianceAdvertisingLabelsRoute = "rules/advertising-labels"
    const val ComplianceAgeGateRoute = "rules/age-gate"
    const val CompliancePrivacyDefaultsRoute = "rules/privacy-defaults"
    const val ComplianceGdprRightsRoute = "rules/gdpr-rights"

    val allRoutes: Set<String> = setOf(
        RulesRoute,
        ComplianceReportBlockHideRoute,
        ComplianceAdvertisingLabelsRoute,
        ComplianceAgeGateRoute,
        CompliancePrivacyDefaultsRoute,
        ComplianceGdprRightsRoute,
    )
}
