package com.example.teamcompass.ui

import android.content.Context
import com.example.teamcompass.R
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure

internal object TeamActionErrorPolicy {
    fun toUserMessage(
        context: Context,
        defaultMessage: String,
        failure: TeamActionFailure,
    ): String {
        val details = failure.message?.lowercase().orEmpty()
        return when (failure.error) {
            TeamActionError.NOT_FOUND -> context.getString(R.string.error_team_code_not_found)
            TeamActionError.LOCKED -> context.getString(R.string.error_match_locked)
            TeamActionError.EXPIRED -> context.getString(R.string.error_match_expired)
            TeamActionError.COLLISION -> context.getString(R.string.error_code_collision)
            TeamActionError.INVALID_INPUT -> context.getString(R.string.error_invalid_input)
            TeamActionError.NETWORK -> context.getString(R.string.error_network_generic)
            TeamActionError.PERMISSION_DENIED -> when {
                "throttled" in details -> context.getString(R.string.error_too_many_requests)
                "expired" in details -> context.getString(R.string.error_match_expired)
                "locked" in details -> context.getString(R.string.error_match_locked)
                else -> context.getString(R.string.error_permission_denied)
            }

            TeamActionError.UNKNOWN -> defaultMessage.ifBlank { failure.message.orEmpty() }
        }
    }
}
