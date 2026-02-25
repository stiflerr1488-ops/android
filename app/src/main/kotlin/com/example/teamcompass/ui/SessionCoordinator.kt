package com.example.teamcompass.ui

import com.example.teamcompass.core.TeamCodeValidator
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamRepository
import kotlinx.coroutines.withTimeoutOrNull

internal class SessionCoordinator(
    private val repository: TeamRepository,
    private val opTimeoutMs: Long,
    private val nowMsProvider: () -> Long = System::currentTimeMillis,
) {
    fun normalizeTeamCode(raw: String?): String? = TeamCodeValidator.normalize(raw)

    suspend fun createTeam(ownerUid: String, ownerCallsign: String): TeamActionResult<String> {
        return withTimeoutOrNull(opTimeoutMs) {
            repository.createTeam(
                ownerUid = ownerUid,
                ownerCallsign = ownerCallsign,
                nowMs = nowMsProvider(),
            )
        } ?: TeamActionResult.Failure(
            TeamActionFailure(
                error = TeamActionError.NETWORK,
                message = "Team creation timed out",
            )
        )
    }

    suspend fun joinTeam(teamCode: String, uid: String, callsign: String): TeamActionResult<Unit> {
        return withTimeoutOrNull(opTimeoutMs) {
            repository.joinTeam(
                teamCode = teamCode,
                uid = uid,
                callsign = callsign,
                nowMs = nowMsProvider(),
            )
        } ?: TeamActionResult.Failure(
            TeamActionFailure(
                error = TeamActionError.NETWORK,
                message = "Team join timed out",
            )
        )
    }

}
