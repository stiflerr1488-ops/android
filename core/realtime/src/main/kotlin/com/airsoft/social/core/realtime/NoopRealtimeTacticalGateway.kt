package com.airsoft.social.core.realtime

import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

object NoopRealtimeTacticalGateway : RealtimeTacticalGateway {
    override fun connectionState(): Flow<RealtimeConnectionState> =
        flowOf(RealtimeConnectionState.Disconnected)

    override fun observeTeamChannel(teamId: String): Flow<RealtimeEvent> = emptyFlow()

    override suspend fun publish(event: RealtimeCommand): AppResult<Unit> =
        AppResult.Failure(AppError.Unsupported)
}

