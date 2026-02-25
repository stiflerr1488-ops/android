package com.airsoft.social.core.realtime

sealed interface RealtimeConnectionState {
    data object Disconnected : RealtimeConnectionState
    data object Connecting : RealtimeConnectionState
    data object Connected : RealtimeConnectionState
    data class Failed(val reason: String) : RealtimeConnectionState
}

sealed interface RealtimeEvent {
    data class TeamPayload(val teamId: String, val payload: Map<String, Any?>) : RealtimeEvent
    data class System(val message: String) : RealtimeEvent
}

sealed interface RealtimeCommand {
    data class PublishTeamPayload(val teamId: String, val payload: Map<String, Any?>) : RealtimeCommand
}

