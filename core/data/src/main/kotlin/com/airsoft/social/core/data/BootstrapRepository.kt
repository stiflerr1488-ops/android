package com.airsoft.social.core.data

import com.airsoft.social.core.model.AppTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface BootstrapRepository {
    fun observeBadgeCounts(): Flow<Map<AppTab, Int>>
}

class FakeBootstrapRepository(
    initialCounts: Map<AppTab, Int> = mapOf(
        AppTab.Chats to 3,
        AppTab.Teams to 1,
        AppTab.Events to 2,
        AppTab.Marketplace to 0,
        AppTab.Profile to 0,
    ),
) : BootstrapRepository {
    private val badgeCounts = MutableStateFlow(initialCounts)

    override fun observeBadgeCounts(): Flow<Map<AppTab, Int>> = badgeCounts.asStateFlow()
}

