package com.airsoft.social.core.realtime

import com.airsoft.social.core.common.AppResult
import kotlinx.coroutines.flow.Flow

interface RealtimeTacticalGateway {
    fun connectionState(): Flow<RealtimeConnectionState>
    fun observeTeamChannel(teamId: String): Flow<RealtimeEvent>
    suspend fun publish(event: RealtimeCommand): AppResult<Unit>
}

