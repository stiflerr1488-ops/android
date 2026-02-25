package com.airsoft.social.core.model

data class FeatureAvailability(
    val key: String,
    val enabled: Boolean,
    val reason: String? = null,
)

