package com.airsoft.social.core.tactical

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

enum class TacticalMigrationStage {
    LegacyBridge,
    HybridBridge,
    NativeFeature,
}

data class TacticalOverviewSnapshot(
    val migrationStage: TacticalMigrationStage = TacticalMigrationStage.LegacyBridge,
    val backendProvider: String = "legacy-firebase-bridge",
    val realtimeConnected: Boolean = false,
    val activeTeamId: String? = null,
    val note: String = "Using legacy tactical mode via bridge while migration is in progress.",
)

interface TacticalOverviewPort {
    fun observeOverview(): Flow<TacticalOverviewSnapshot>
}

object NoopTacticalOverviewPort : TacticalOverviewPort {
    override fun observeOverview(): Flow<TacticalOverviewSnapshot> = flowOf(TacticalOverviewSnapshot())
}

class FakeTacticalOverviewPort(
    initial: TacticalOverviewSnapshot = TacticalOverviewSnapshot(),
) : TacticalOverviewPort {
    private val snapshot = MutableStateFlow(initial)

    override fun observeOverview(): Flow<TacticalOverviewSnapshot> = snapshot.asStateFlow()

    fun update(transform: (TacticalOverviewSnapshot) -> TacticalOverviewSnapshot) {
        snapshot.value = transform(snapshot.value)
    }
}
