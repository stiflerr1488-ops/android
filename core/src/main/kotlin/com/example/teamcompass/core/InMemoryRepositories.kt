package com.example.teamcompass.core

import java.util.UUID

class InMemoryMatchRepository(
    private val stateRepository: StateRepository,
) : MatchRepository {
    private val metas = mutableMapOf<String, MatchMeta>()
    private val members = mutableMapOf<String, MutableList<Member>>()

    override fun createMatch(ownerUid: String, ownerNick: String, nowMs: Long): MatchMeta {
        val matchId = generateUniqueMatchId()
        val meta = MatchMeta(
            matchId = matchId,
            createdBy = ownerUid,
            createdAtMs = nowMs,
            expiresAtMs = nowMs + 12 * 60 * 60 * 1000,
            isLocked = false,
        )
        metas[matchId] = meta
        members[matchId] = mutableListOf(Member(ownerUid, ownerNick, nowMs))
        return meta
    }

    override fun joinMatch(matchId: String, uid: String, nick: String, nowMs: Long): Member {
        val meta = metas[matchId] ?: error("Unknown matchId")

        members[matchId]
            ?.firstOrNull { it.uid == uid }
            ?.let { return it }

        require(!meta.isLocked) { "Match is locked" }
        require(nowMs <= meta.expiresAtMs) { "Match expired" }

        val existingNicks = members[matchId].orEmpty().map { it.nick }.toSet()
        val resolvedNick = resolveUniqueNick(nick, existingNicks)
        val member = Member(uid, resolvedNick, nowMs)
        members.getOrPut(matchId) { mutableListOf() }.add(member)
        return member
    }

    override fun lockMatch(matchId: String) {
        metas.computeIfPresent(matchId) { _, value -> value.copy(isLocked = true) }
    }

    override fun closeMatch(matchId: String) {
        metas.remove(matchId)
        members.remove(matchId)
    }

    override fun getSnapshot(matchId: String): MatchSnapshot? {
        val meta = metas[matchId] ?: return null
        return MatchSnapshot(
            meta = meta,
            members = members[matchId].orEmpty().toList(),
            states = stateRepository.listStates(matchId),
        )
    }

    private fun resolveUniqueNick(base: String, occupied: Set<String>): String {
        if (base !in occupied) return base
        var idx = 2
        while ("$base#$idx" in occupied) idx++
        return "$base#$idx"
    }

    private fun generateUniqueMatchId(): String {
        repeat(MAX_MATCH_ID_GENERATION_ATTEMPTS) {
            val matchId = UUID.randomUUID().toString().take(8)
            if (!metas.containsKey(matchId)) {
                return matchId
            }
        }
        error("Failed to generate unique match id")
    }

    companion object {
        private const val MAX_MATCH_ID_GENERATION_ATTEMPTS = 1_024
    }
}

class InMemoryStateRepository : StateRepository {
    private val map = mutableMapOf<String, MutableMap<String, PlayerState>>()

    override fun upsertState(matchId: String, state: PlayerState) {
        map.getOrPut(matchId) { mutableMapOf() }[state.uid] = state
    }

    override fun listStates(matchId: String): List<PlayerState> =
        map[matchId].orEmpty().values.toList()
}
