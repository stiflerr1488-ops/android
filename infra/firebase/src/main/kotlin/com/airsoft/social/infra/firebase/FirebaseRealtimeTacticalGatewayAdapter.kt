package com.airsoft.social.infra.firebase

import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.realtime.RealtimeCommand
import com.airsoft.social.core.realtime.RealtimeConnectionState
import com.airsoft.social.core.realtime.RealtimeEvent
import com.airsoft.social.core.realtime.RealtimeTacticalGateway
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FirebaseRealtimeTacticalGatewayAdapter(
    private val firebaseDatabaseProvider: () -> FirebaseDatabase? = {
        runCatching { FirebaseDatabase.getInstance() }.getOrNull()
    },
) : RealtimeTacticalGateway {
    override fun connectionState(): Flow<RealtimeConnectionState> =
        flowOf(
            if (firebaseDatabaseProvider() != null) {
                RealtimeConnectionState.Connected
            } else {
                RealtimeConnectionState.Disconnected
            },
        )

    override fun observeTeamChannel(teamId: String): Flow<RealtimeEvent> = emptyFlow()

    override suspend fun publish(event: RealtimeCommand): AppResult<Unit> = when (event) {
        is RealtimeCommand.PublishTeamPayload -> AppResult.Failure(
            AppError.Unsupported,
        )
    }
}

