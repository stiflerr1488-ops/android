package com.example.teamcompass.ui

import androidx.annotation.StringRes
import com.example.teamcompass.R
import com.example.teamcompass.domain.TeamActionError

internal data class TeamListeningTerminalFailure(
    @StringRes val userMessageResId: Int,
    val logReason: String,
)

internal object TeamListeningFailurePolicy {
    fun resolve(error: TeamActionError): TeamListeningTerminalFailure? {
        return when (error) {
            TeamActionError.LOCKED -> TeamListeningTerminalFailure(
                userMessageResId = R.string.vm_error_team_locked_format,
                logReason = "team locked",
            )

            TeamActionError.EXPIRED -> TeamListeningTerminalFailure(
                userMessageResId = R.string.vm_error_team_code_expired_format,
                logReason = "team expired",
            )

            TeamActionError.NOT_FOUND -> TeamListeningTerminalFailure(
                userMessageResId = R.string.vm_error_team_not_found_format,
                logReason = "team not found",
            )

            TeamActionError.PERMISSION_DENIED -> TeamListeningTerminalFailure(
                userMessageResId = R.string.vm_error_team_permission_denied_format,
                logReason = "permission denied",
            )

            else -> null
        }
    }
}
