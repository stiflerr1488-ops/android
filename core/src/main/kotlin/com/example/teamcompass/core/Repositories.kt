package com.example.teamcompass.core

interface MatchRepository {
    fun createMatch(ownerUid: String, ownerNick: String, nowMs: Long): MatchMeta
    fun joinMatch(matchId: String, uid: String, nick: String, nowMs: Long): Member
    fun lockMatch(matchId: String)
    fun closeMatch(matchId: String)
    fun getSnapshot(matchId: String): MatchSnapshot?

    // Управление ролями
    fun assignRole(matchId: String, targetUid: String, role: Role, actorUid: String, actorRole: Role): Result<Unit>
    fun getRole(matchId: String, uid: String): Role?
    fun kickMember(matchId: String, targetUid: String, actorUid: String, actorRole: Role): Result<Unit>
}

interface StateRepository {
    fun upsertState(matchId: String, state: PlayerState)
    fun listStates(matchId: String): List<PlayerState>
}
