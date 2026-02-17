package com.example.teamcompass.core

interface MatchRepository {
    fun createMatch(ownerUid: String, ownerNick: String, nowMs: Long): MatchMeta
    fun joinMatch(matchId: String, uid: String, nick: String, nowMs: Long): Member
    fun lockMatch(matchId: String)
    fun closeMatch(matchId: String)
    fun getSnapshot(matchId: String): MatchSnapshot?
}

interface StateRepository {
    fun upsertState(matchId: String, state: PlayerState)
    fun listStates(matchId: String): List<PlayerState>
    fun clearStates(matchId: String)
}
