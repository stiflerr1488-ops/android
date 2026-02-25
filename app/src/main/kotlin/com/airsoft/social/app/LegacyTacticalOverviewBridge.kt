package com.airsoft.social.app

import com.airsoft.social.core.tactical.TacticalMigrationStage
import com.airsoft.social.core.tactical.TacticalOverviewPort
import com.airsoft.social.core.tactical.TacticalOverviewSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LegacyTacticalOverviewBridge : TacticalOverviewPort {
    private val snapshot = MutableStateFlow(TacticalOverviewSnapshot())

    override fun observeOverview(): Flow<TacticalOverviewSnapshot> = snapshot.asStateFlow()

    fun onLegacyShellStateChanged(
        isAuthReady: Boolean,
        teamCode: String?,
        backendAvailable: Boolean,
        isBackendStale: Boolean,
    ) {
        snapshot.value = snapshot.value.copy(
            migrationStage = TacticalMigrationStage.LegacyBridge,
            backendProvider = "legacy-firebase-rtdb",
            realtimeConnected = isAuthReady && teamCode != null && backendAvailable && !isBackendStale,
            activeTeamId = teamCode,
            note = buildNote(
                isAuthReady = isAuthReady,
                teamCode = teamCode,
                backendAvailable = backendAvailable,
                isBackendStale = isBackendStale,
            ),
        )
    }

    private fun buildNote(
        isAuthReady: Boolean,
        teamCode: String?,
        backendAvailable: Boolean,
        isBackendStale: Boolean,
    ): String = when {
        !isAuthReady -> "Legacy tactical is waiting for authentication readiness."
        teamCode.isNullOrBlank() -> "Legacy tactical is active. Join or create a team to start realtime sync."
        !backendAvailable -> "Legacy tactical backend is unavailable. Firebase retries are in progress."
        isBackendStale -> "Legacy tactical backend is stale. Showing last-known state while waiting for fresh updates."
        else -> "Legacy tactical mode is active through the bridge."
    }
}
