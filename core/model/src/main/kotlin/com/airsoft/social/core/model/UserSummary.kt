package com.airsoft.social.core.model

data class UserSummary(
    val id: String,
    val callsign: String,
    val avatarUrl: String? = null,
)

