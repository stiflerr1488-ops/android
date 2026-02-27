package com.airsoft.social.feature.chats.impl

import androidx.compose.runtime.Composable
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.chats.api.ChatsFeatureApi

@Composable
fun ChatsPlaceholderScreen() {
    WireframePage(
        title = ChatsFeatureApi.contract.title,
        subtitle = "Чаты показывают только реальные данные из backend. Пока данных нет.",
    ) {
        WireframeSection(title = "Диалоги") {}
        WireframeSection(title = "Игроки рядом") {}
    }
}

