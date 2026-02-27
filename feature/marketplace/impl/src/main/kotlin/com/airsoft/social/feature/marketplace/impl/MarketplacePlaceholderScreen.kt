package com.airsoft.social.feature.marketplace.impl

import androidx.compose.runtime.Composable
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi

@Composable
fun MarketplacePlaceholderScreen() {
    WireframePage(
        title = MarketplaceFeatureApi.contract.title,
        subtitle = "Листинги и продажи отображаются только из backend. Заглушки удалены.",
    ) {
        WireframeSection(title = "Лента объявлений") {}
        WireframeSection(title = "Мои объявления") {}
    }
}

