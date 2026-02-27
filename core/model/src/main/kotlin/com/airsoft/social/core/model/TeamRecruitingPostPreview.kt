package com.airsoft.social.core.model

data class TeamRecruitingPostPreview(
    val id: String,
    val teamId: String,
    val title: String,
    val subtitle: String,
    val actionLabel: String? = null,
    val tags: Set<String> = emptySet(),
)
