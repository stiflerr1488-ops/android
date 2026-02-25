package com.example.teamcompass.ui

import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.p2p.P2PDispatchReport
import com.example.teamcompass.p2p.P2PInboundMessage
import com.example.teamcompass.p2p.P2PTransportManager
import com.example.teamcompass.core.p2p.P2PMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter

internal sealed interface TeamListeningPreflightResult {
    data class Ready(
        val teamCode: String,
    ) : TeamListeningPreflightResult

    data class InvalidCode(
        val rawCode: String,
    ) : TeamListeningPreflightResult

    data class JoinFailure(
        val teamCode: String,
        val failure: TeamActionFailure,
    ) : TeamListeningPreflightResult
}

internal class TeamSessionDelegate(
    private val sessionCoordinator: SessionCoordinator,
    private val p2pTransportManager: P2PTransportManager? = null,
) {
    fun normalizeTeamCode(raw: String?): String? = sessionCoordinator.normalizeTeamCode(raw)

    suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
    ): TeamActionResult<String> {
        return sessionCoordinator.createTeam(
            ownerUid = ownerUid,
            ownerCallsign = ownerCallsign,
        )
    }

    suspend fun joinTeam(
        teamCode: String,
        uid: String,
        callsign: String,
    ): TeamActionResult<Unit> {
        return sessionCoordinator.joinTeam(
            teamCode = teamCode,
            uid = uid,
            callsign = callsign,
        )
    }

    suspend fun preflightStartListening(
        codeRaw: String,
        uid: String,
        callsign: String,
    ): TeamListeningPreflightResult {
        val teamCode = normalizeTeamCode(codeRaw)
            ?: return TeamListeningPreflightResult.InvalidCode(rawCode = codeRaw)
        return when (val joinResult = joinTeam(teamCode = teamCode, uid = uid, callsign = callsign)) {
            is TeamActionResult.Success -> TeamListeningPreflightResult.Ready(teamCode = teamCode)
            is TeamActionResult.Failure -> TeamListeningPreflightResult.JoinFailure(
                teamCode = teamCode,
                failure = joinResult.details,
            )
        }
    }

    suspend fun broadcastP2P(message: P2PMessage): P2PDispatchReport? {
        return p2pTransportManager?.broadcast(message)
    }

    fun observeP2PInbound(teamCode: String): Flow<P2PInboundMessage> {
        val manager = p2pTransportManager ?: return emptyFlow()
        return manager.receiveAll().filter { inbound ->
            inbound.message.metadata.teamCode == teamCode
        }
    }
}
