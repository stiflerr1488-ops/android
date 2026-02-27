package com.airsoft.social.feature.teams.impl

import androidx.compose.runtime.Composable
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.teams.api.TeamsFeatureApi

@Composable
fun TeamsPlaceholderScreen() {
    WireframePage(
        title = TeamsFeatureApi.contract.title,
        subtitle = "Список команд, состав и рекрутинг будут отображаться только из backend.",
    ) {
        WireframeSection(title = "Моя команда") {}
        WireframeSection(title = "Ростер") {}
        WireframeSection(title = "Рекрутинг") {}
    }
}

