package com.example.teamcompass.ui

internal class JoinRateLimiter(
    private val nowMs: () -> Long = System::currentTimeMillis,
    private val windowMs: Long = 60_000L,
    private val maxAttemptsPerCode: Int = 5,
    private val maxAttemptsGlobal: Int = 20,
) {
    private val lock = Any()
    private val attemptsByCode = linkedMapOf<String, ArrayDeque<Long>>()
    private val globalAttempts = ArrayDeque<Long>()

    fun canAttempt(teamCode: String): Boolean {
        synchronized(lock) {
            val normalizedCode = teamCode.trim()
            if (normalizedCode.isBlank()) return true

            val now = nowMs()
            pruneQueue(globalAttempts, now)
            if (globalAttempts.size >= maxAttemptsGlobal.coerceAtLeast(1)) {
                return false
            }

            val codeAttempts = attemptsByCode.getOrPut(normalizedCode) { ArrayDeque() }
            pruneQueue(codeAttempts, now)
            if (codeAttempts.size >= maxAttemptsPerCode.coerceAtLeast(1)) {
                return false
            }

            codeAttempts.addLast(now)
            globalAttempts.addLast(now)
            return true
        }
    }

    fun reset() {
        synchronized(lock) {
            attemptsByCode.clear()
            globalAttempts.clear()
        }
    }

    private fun pruneQueue(queue: ArrayDeque<Long>, nowMs: Long) {
        while (queue.isNotEmpty() && nowMs - queue.first() > windowMs) {
            queue.removeFirst()
        }
    }
}
