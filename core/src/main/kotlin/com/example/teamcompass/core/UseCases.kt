package com.example.teamcompass.core

class StartMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    operator fun invoke(ownerUid: String, ownerNick: String, nowMs: Long): MatchMeta =
        matchRepository.createMatch(ownerUid, ownerNick, nowMs)
}

class JoinMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    operator fun invoke(matchId: String, uid: String, nick: String, nowMs: Long): Member =
        matchRepository.joinMatch(matchId, uid, nick, nowMs)
}

class StartTrackingUseCase {
    operator fun invoke(mode: TrackingMode): TrackingPolicy = TrackingPolicies.forMode(mode)
}

class StopTrackingUseCase {
    operator fun invoke(): Boolean = true
}
