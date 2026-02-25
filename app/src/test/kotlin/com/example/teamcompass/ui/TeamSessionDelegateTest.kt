package com.example.teamcompass.ui

import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PMessageMetadata
import com.example.teamcompass.core.p2p.P2PMessageType
import com.example.teamcompass.core.p2p.P2PPriority
import com.example.teamcompass.core.p2p.P2PTransport
import com.example.teamcompass.core.p2p.P2PTransportCapability
import com.example.teamcompass.core.p2p.P2PTransportLimits
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamPointPayload
import com.example.teamcompass.domain.TeamPointUpdatePayload
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamSnapshot
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TeamViewMode
import com.example.teamcompass.p2p.P2PTransportManager
import com.example.teamcompass.p2p.P2PTransportRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamSessionDelegateTest {

    @Test
    fun preflightStartListening_invalidCode_returnsInvalidCode() = runTest {
        val delegate = createDelegate(TeamSessionFakeRepository())

        val result = delegate.preflightStartListening(
            codeRaw = "12A456",
            uid = "uid-1",
            callsign = "Alpha",
        )

        assertTrue(result is TeamListeningPreflightResult.InvalidCode)
    }

    @Test
    fun preflightStartListening_joinFailure_returnsJoinFailure() = runTest {
        val repository = TeamSessionFakeRepository().apply {
            joinTeamResult = TeamActionResult.Failure(
                TeamActionFailure(
                    error = TeamActionError.PERMISSION_DENIED,
                    message = "denied",
                )
            )
        }
        val delegate = createDelegate(repository)

        val result = delegate.preflightStartListening(
            codeRaw = "123456",
            uid = "uid-2",
            callsign = "Bravo",
        )

        assertTrue(result is TeamListeningPreflightResult.JoinFailure)
        result as TeamListeningPreflightResult.JoinFailure
        assertEquals("123456", result.teamCode)
        assertEquals(TeamActionError.PERMISSION_DENIED, result.failure.error)
    }

    @Test
    fun preflightStartListening_success_returnsReady_withNormalizedCode() = runTest {
        val repository = TeamSessionFakeRepository()
        val delegate = createDelegate(repository)

        val result = delegate.preflightStartListening(
            codeRaw = "123456",
            uid = "uid-3",
            callsign = "Charlie",
        )

        assertTrue(result is TeamListeningPreflightResult.Ready)
        result as TeamListeningPreflightResult.Ready
        assertEquals("123456", result.teamCode)
        assertEquals("123456", repository.lastJoinTeamCode)
        assertEquals("uid-3", repository.lastJoinUid)
        assertEquals("Charlie", repository.lastJoinCallsign)
    }

    @Test
    fun observeP2PInbound_filters_by_team_code() = runTest {
        val transport = TeamSessionFakeP2PTransport()
        val manager = P2PTransportManager(
            transportRegistry = P2PTransportRegistry(listOf(transport)),
        )
        val delegate = createDelegate(
            repository = TeamSessionFakeRepository(),
            p2pTransportManager = manager,
        )
        val collected = mutableListOf<com.example.teamcompass.p2p.P2PInboundMessage>()

        val job = launch {
            delegate.observeP2PInbound(teamCode = "123456")
                .take(1)
                .toList(collected)
        }
        runCurrent()

        transport.emitIncoming(message(teamCode = "000001", sequence = 1))
        transport.emitIncoming(message(teamCode = "123456", sequence = 2))

        advanceUntilIdle()

        assertEquals(1, collected.size)
        assertEquals("123456", collected.single().message.metadata.teamCode)
        job.cancel()
    }

    private fun createDelegate(
        repository: TeamSessionFakeRepository,
        p2pTransportManager: P2PTransportManager? = null,
    ): TeamSessionDelegate {
        return TeamSessionDelegate(
            sessionCoordinator = SessionCoordinator(
                repository = repository,
                opTimeoutMs = 500L,
                nowMsProvider = { 123L },
            ),
            p2pTransportManager = p2pTransportManager,
        )
    }

    private fun message(teamCode: String, sequence: Int): P2PMessage {
        return P2PMessage(
            metadata = P2PMessageMetadata(
                type = P2PMessageType.POSITION_UPDATE,
                senderId = "peer-1",
                teamCode = teamCode,
                timestampMs = 1_000L,
                sequenceNumber = sequence,
                ttl = 3,
                priority = P2PPriority.LOW,
            ),
            payload = byteArrayOf(0x01),
        )
    }
}

private class TeamSessionFakeRepository : TeamRepository {
    var joinTeamResult: TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    var lastJoinTeamCode: String? = null
    var lastJoinUid: String? = null
    var lastJoinCallsign: String? = null

    override suspend fun createTeam(
        ownerUid: String,
        ownerCallsign: String,
        nowMs: Long,
        maxAttempts: Int,
    ): TeamActionResult<String> = TeamActionResult.Success("123456")

    override suspend fun joinTeam(
        teamCode: String,
        uid: String,
        callsign: String,
        nowMs: Long,
    ): TeamActionResult<Unit> {
        lastJoinTeamCode = teamCode
        lastJoinUid = uid
        lastJoinCallsign = callsign
        return joinTeamResult
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

private class TeamSessionFakeP2PTransport : P2PTransport {
    private val incoming = MutableSharedFlow<P2PMessage>(extraBufferCapacity = 16)

    override val name: String = "fake-p2p"
    override val limits: P2PTransportLimits = P2PTransportLimits(maxPayloadBytes = 256)
    override val capabilities: Set<P2PTransportCapability> = setOf(
        P2PTransportCapability.BROADCAST,
        P2PTransportCapability.ACKS,
    )

    override suspend fun send(peerId: String, message: P2PMessage): Result<Unit> = Result.success(Unit)

    override suspend fun broadcast(message: P2PMessage): Result<Unit> = Result.success(Unit)

    override fun receive(): Flow<P2PMessage> = incoming

    override fun connectedPeers(): Set<String> = setOf("peer-1")

    suspend fun emitIncoming(message: P2PMessage) {
        incoming.emit(message)
    }
}
