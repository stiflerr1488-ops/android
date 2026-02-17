package com.example.teamcompass.core

data class MatchMeta(
    val matchId: String,
    val createdBy: String,
    val createdAtMs: Long,
    val expiresAtMs: Long,
    val isLocked: Boolean,
)

data class Member(
    val uid: String,
    val nick: String,
    val joinedAtMs: Long,
)

data class MatchSnapshot(
    val meta: MatchMeta,
    val members: List<Member>,
    val states: List<PlayerState>,
)
