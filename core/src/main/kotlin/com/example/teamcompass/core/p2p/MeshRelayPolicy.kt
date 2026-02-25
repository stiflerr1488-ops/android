package com.example.teamcompass.core.p2p

import kotlin.random.Random

enum class MeshDropReason {
    EXPIRED_TTL,
    DUPLICATE,
    RATE_LIMIT,
}

data class MeshRelayDecision(
    val shouldForward: Boolean,
    val nextTtl: Int,
    val jitterDelayMs: Long,
    val dropReason: MeshDropReason? = null,
)

class MeshRelayPolicy(
    private val replayProtector: ReplayProtector,
    private val minRelayIntervalMs: Long = 250L,
    private val jitterMaxMs: Long = 150L,
    private val nowMsProvider: () -> Long = System::currentTimeMillis,
    private val jitterProvider: (Long) -> Long = { max ->
        if (max <= 0L) 0L else Random.nextLong(from = 0L, until = max + 1L)
    },
) {
    private var lastRelayAtMs: Long = Long.MIN_VALUE

    init {
        require(minRelayIntervalMs >= 0L) { "minRelayIntervalMs must be >= 0" }
        require(jitterMaxMs >= 0L) { "jitterMaxMs must be >= 0" }
    }

    @Synchronized
    fun decide(frame: MeshFrame): MeshRelayDecision {
        if (frame.ttl <= 0) {
            return MeshRelayDecision(
                shouldForward = false,
                nextTtl = 0,
                jitterDelayMs = 0L,
                dropReason = MeshDropReason.EXPIRED_TTL,
            )
        }

        val dedupAccepted = replayProtector.shouldAccept(
            senderId = frame.originalSenderId,
            sequenceNumber = frame.sequenceNumber,
        )
        if (!dedupAccepted) {
            return MeshRelayDecision(
                shouldForward = false,
                nextTtl = frame.ttl,
                jitterDelayMs = 0L,
                dropReason = MeshDropReason.DUPLICATE,
            )
        }

        val nowMs = nowMsProvider()
        if (lastRelayAtMs != Long.MIN_VALUE && nowMs - lastRelayAtMs < minRelayIntervalMs) {
            return MeshRelayDecision(
                shouldForward = false,
                nextTtl = frame.ttl,
                jitterDelayMs = 0L,
                dropReason = MeshDropReason.RATE_LIMIT,
            )
        }

        lastRelayAtMs = nowMs
        return MeshRelayDecision(
            shouldForward = true,
            nextTtl = (frame.ttl - 1).coerceAtLeast(0),
            jitterDelayMs = jitterProvider(jitterMaxMs),
        )
    }
}
