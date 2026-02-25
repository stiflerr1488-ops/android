package com.example.teamcompass.core.p2p

data class ReplayKey(
    val senderId: String,
    val sequenceNumber: Int,
)

class ReplayProtector(
    private val ttlMs: Long = 120_000L,
    private val maxEntries: Int = 4_096,
    private val nowMsProvider: () -> Long = System::currentTimeMillis,
) {
    private val seenAtByKey = LinkedHashMap<ReplayKey, Long>()

    init {
        require(ttlMs > 0L) { "ttlMs must be > 0" }
        require(maxEntries > 0) { "maxEntries must be > 0" }
    }

    @Synchronized
    fun shouldAccept(senderId: String, sequenceNumber: Int): Boolean {
        require(senderId.isNotBlank()) { "senderId must not be blank" }
        require(sequenceNumber >= 0) { "sequenceNumber must be >= 0" }
        val nowMs = nowMsProvider()
        pruneExpiredLocked(nowMs)
        val key = ReplayKey(senderId = senderId, sequenceNumber = sequenceNumber)
        if (seenAtByKey.containsKey(key)) return false
        seenAtByKey[key] = nowMs
        trimToMaxEntriesLocked()
        return true
    }

    @Synchronized
    fun size(): Int = seenAtByKey.size

    @Synchronized
    fun pruneExpired(nowMs: Long = nowMsProvider()): Int = pruneExpiredLocked(nowMs)

    private fun trimToMaxEntriesLocked() {
        while (seenAtByKey.size > maxEntries) {
            val oldestKey = seenAtByKey.entries.firstOrNull()?.key ?: return
            seenAtByKey.remove(oldestKey)
        }
    }

    private fun pruneExpiredLocked(nowMs: Long): Int {
        val iterator = seenAtByKey.entries.iterator()
        var removed = 0
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (nowMs - entry.value > ttlMs) {
                iterator.remove()
                removed++
            }
        }
        return removed
    }
}
