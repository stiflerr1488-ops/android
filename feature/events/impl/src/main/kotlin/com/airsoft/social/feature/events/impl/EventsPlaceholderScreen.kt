package com.airsoft.social.feature.events.impl

import androidx.compose.runtime.Composable
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.events.api.EventsFeatureApi

@Composable
fun EventsPlaceholderScreen() {
    WireframePage(
        title = EventsFeatureApi.contract.title,
        subtitle = "Календарь и события показываются только при наличии реальных данных.",
    ) {
        WireframeSection(title = "Ближайшие игры") {}
        WireframeSection(title = "Мои регистрации") {}
    }
}

