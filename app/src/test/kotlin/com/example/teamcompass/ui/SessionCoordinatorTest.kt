package com.example.teamcompass.ui

import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionCoordinatorTest {

    @Test
    fun normalizeTeamCode_accepts_only_exact_six_digits() {
        val coordinator = SessionCoordinator(SessionFakeTeamRepository(), opTimeoutMs = 100)

        assertEquals("123456", coordinator.normalizeTeamCode("123456"))
        assertNull(coordinator.normalizeTeamCode("12345"))
        assertNull(coordinator.normalizeTeamCode("0123456"))
        assertNull(coordinator.normalizeTeamCode("12A456"))
    }

    @Test
    fun createTeam_delegates_parameters_and_nowMs() = runTest {
        val repository = SessionFakeTeamRepository()
        val coordinator = SessionCoordinator(
            repository = repository,
            opTimeoutMs = 1_000,
            nowMsProvider = { 42L },
        )

        val result = coordinator.createTeam(ownerUid = "u-1", ownerCallsign = "Alpha")

        assertTrue(result is TeamActionResult.Success)
        assertEquals("u-1", repository.lastCreateUid)
        assertEquals("Alpha", repository.lastCreateCallsign)
        assertEquals(42L, repository.lastCreateNowMs)
    }

    @Test
    fun joinTeam_timeout_returns_network_failure() = runTest {
        val repository = SessionFakeTeamRepository().apply {
            joinDelayMs = 200L
        }
        val coordinator = SessionCoordinator(repository = repository, opTimeoutMs = 50L)

        val result = coordinator.joinTeam(teamCode = "123456", uid = "u-1", callsign = "Alpha")

        assertTrue(result is TeamActionResult.Failure)
        val failure = (result as TeamActionResult.Failure).details
        assertEquals(TeamActionError.NETWORK, failure.error)
        assertTrue(failure.message.orEmpty().isNotBlank())
    }

    @Test
    fun joinTeam_delegates_parameters_and_nowMs() = runTest {
        val repository = SessionFakeTeamRepository()
        val coordinator = SessionCoordinator(
            repository = repository,
            opTimeoutMs = 1_000,
            nowMsProvider = { 77L },
        )

        val result = coordinator.joinTeam(teamCode = "123456", uid = "u-join", callsign = "Joiner")

        assertTrue(result is TeamActionResult.Success)
        assertEquals("123456", repository.lastJoinTeamCode)
        assertEquals("u-join", repository.lastJoinUid)
        assertEquals("Joiner", repository.lastJoinCallsign)
        assertEquals(77L, repository.lastJoinNowMs)
    }
}

private class SessionFakeTeamRepository : TeamRepository {
    var createDelayMs: Long = 0L
    var joinDelayMs: Long = 0L

    var lastCreateUid: String? = null
    var lastCreateCallsign: String? = null
    var lastCreateNowMs: Long? = null
    var lastJoinTeamCode: String? = null
    var lastJoinUid: String? = null
    var lastJoinCallsign: String? = null
    var lastJoinNowMs: Long? = null

    override suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
        nowMs: Long,
        maxAttempts: Int,
    ): TeamActionResult<String> {
        if (createDelayMs > 0L) delay(createDelayMs)
        lastCreateUid = ownerUid
        lastCreateCallsign = ownerCallsign
        lastCreateNowMs = nowMs
        return TeamActionResult.Success("123456")
    }

    override suspend fun joinTeam(
        teamCode: String,
        uid: String,
        callsign: String,
        nowMs: Long,
    ): TeamActionResult<Unit> {
        if (joinDelayMs > 0L) delay(joinDelayMs)
        lastJoinTeamCode = teamCode
        lastJoinUid = uid
        lastJoinCallsign = callsign
        lastJoinNowMs = nowMs
        return TeamActionResult.Success(Unit)
    }

    override fun observeTeam(
        teamCode: String,
        uid: String,
        viewMode: TeamViewMode,
        selfPoint: com.example.teamcompass.core.LocationPoint?,
    ): Flow<TeamSnapshot> = emptyFlow()

    override suspend fun upsertState(teamCode: String, uid: String, payload: TeamStatePayload): TeamActionResult<Unit> {
        return TeamActionResult.Success(Unit)
    }

    override suspend fun addPoint(
        teamCode: String,
        uid: String,
        payload: TeamPointPayload,
        forTeam: Boolean,
    ): TeamActionResult<String> = TeamActionResult.Success("p-1")

    override suspend fun updatePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        payload: TeamPointUpdatePayload,
        isTeam: Boolean,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override suspend fun deletePoint(
        teamCode: String,
        uid: String,
        pointId: String,
        isTeam: Boolean,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override suspend fun setActiveCommand(teamCode: String, uid: String, type: String): TeamActionResult<Unit> {
        return TeamActionResult.Success(Unit)
    }

    override suspend fun addEnemyPing(
        teamCode: String,
        uid: String,
        lat: Double,
        lon: Double,
        type: String,
        ttlMs: Long,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)

    override fun observeMemberPrefs(teamCode: String, uid: String): Flow<TeamMemberPrefs?> = emptyFlow()

    override suspend fun upsertMemberPrefs(
        teamCode: String,
        uid: String,
        prefs: TeamMemberPrefs,
    ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)
}
