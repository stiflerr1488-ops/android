package com.example.teamcompass.core

import java.util.UUID

class InMemoryMatchRepository(
    private val stateRepository: StateRepository,
) : MatchRepository {
    private val metas = mutableMapOf<String, MatchMeta>()
    private val members = mutableMapOf<String, MutableList<Member>>()
    private val roles = mutableMapOf<String, MutableMap<String, Role>>()

    override fun createMatch(ownerUid: String, ownerNick: String, nowMs: Long): MatchMeta {
        val matchId = UUID.randomUUID().toString().take(8)
        val meta = MatchMeta(
            matchId = matchId,
            createdBy = ownerUid,
            createdAtMs = nowMs,
            expiresAtMs = nowMs + 12 * 60 * 60 * 1000,
            isLocked = false,
        )
        metas[matchId] = meta
        members[matchId] = mutableListOf(Member(ownerUid, ownerNick, nowMs, Role.COMMANDER))
        roles.getOrPut(matchId) { mutableMapOf() }[ownerUid] = Role.COMMANDER
        return meta
    }

    override fun joinMatch(matchId: String, uid: String, nick: String, nowMs: Long): Member {
        val meta = metas[matchId] ?: error("Unknown matchId")
        require(!meta.isLocked) { "Match is locked" }
        require(nowMs <= meta.expiresAtMs) { "Match expired" }

        val existingNicks = members[matchId].orEmpty().map { it.nick }.toSet()
        val resolvedNick = resolveUniqueNick(nick, existingNicks)
        val member = Member(uid, resolvedNick, nowMs, Role.FIGHTER)
        members.getOrPut(matchId) { mutableListOf() }.add(member)
        roles.getOrPut(matchId) { mutableMapOf() }[uid] = Role.FIGHTER
        return member
    }

    override fun lockMatch(matchId: String) {
        metas.computeIfPresent(matchId) { _, value -> value.copy(isLocked = true) }
    }

    override fun closeMatch(matchId: String) {
        metas.remove(matchId)
        members.remove(matchId)
        roles.remove(matchId)
    }

    override fun getSnapshot(matchId: String): MatchSnapshot? {
        val meta = metas[matchId] ?: return null
        return MatchSnapshot(
            meta = meta,
            members = members[matchId].orEmpty().toList(),
            states = stateRepository.listStates(matchId),
        )
    }

    override fun assignRole(matchId: String, targetUid: String, role: Role, actorUid: String, actorRole: Role): Result<Unit> {
        val matchRoles = roles[matchId] ?: return Result.failure(Exception("Match not found"))

        // Only commander can assign roles
        if (!actorRole.canCommand()) {
            return Result.failure(SecurityException("Only commander can assign roles"))
        }

        // Cannot assign a role higher than actor's own role
        if (role.priority > actorRole.priority) {
            return Result.failure(SecurityException("Cannot assign role above actor role"))
        }

        matchRoles[targetUid] = role
        members[matchId]?.find { it.uid == targetUid }?.let {
            members[matchId]!!.remove(it)
            members[matchId]!!.add(it.copy(role = role))
        }
        return Result.success(Unit)
    }

    override fun getRole(matchId: String, uid: String): Role? {
        return roles[matchId]?.get(uid)
    }

    override fun kickMember(matchId: String, targetUid: String, actorUid: String, actorRole: Role): Result<Unit> {
        val matchRoles = roles[matchId] ?: return Result.failure(Exception("Match not found"))
        val targetRole = matchRoles[targetUid]

        // Commander or deputy can remove members
        if (!actorRole.canDeputy()) {
            return Result.failure(SecurityException("Only commander or deputy can remove members"))
        }

        // Cannot remove a member with equal/higher role unless actor is commander
        if (targetRole != null && targetRole.priority >= actorRole.priority && actorRole != Role.COMMANDER) {
            return Result.failure(SecurityException("Cannot remove member with equal or higher role"))
        }

        members[matchId]?.removeIf { it.uid == targetUid }
        matchRoles.remove(targetUid)
        return Result.success(Unit)
    }

    private fun resolveUniqueNick(base: String, occupied: Set<String>): String {
        if (base !in occupied) return base
        var idx = 2
        while ("$base#$idx" in occupied) idx++
        return "$base#$idx"
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
