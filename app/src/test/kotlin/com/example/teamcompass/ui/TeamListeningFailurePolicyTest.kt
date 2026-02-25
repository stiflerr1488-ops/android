package com.example.teamcompass.ui

import com.example.teamcompass.R
import com.example.teamcompass.domain.TeamActionError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TeamListeningFailurePolicyTest {

    @Test
    fun resolve_maps_terminal_errors_to_expected_user_messages() {
        assertPolicy(
            error = TeamActionError.LOCKED,
            expectedResId = R.string.vm_error_team_locked_format,
            expectedReason = "team locked",
        )
        assertPolicy(
            error = TeamActionError.EXPIRED,
            expectedResId = R.string.vm_error_team_code_expired_format,
            expectedReason = "team expired",
        )
        assertPolicy(
            error = TeamActionError.NOT_FOUND,
            expectedResId = R.string.vm_error_team_not_found_format,
            expectedReason = "team not found",
        )
        assertPolicy(
            error = TeamActionError.PERMISSION_DENIED,
            expectedResId = R.string.vm_error_team_permission_denied_format,
            expectedReason = "permission denied",
        )
    }

    @Test
    fun resolve_returns_null_for_non_terminal_errors() {
        assertNull(TeamListeningFailurePolicy.resolve(TeamActionError.NETWORK))
        assertNull(TeamListeningFailurePolicy.resolve(TeamActionError.COLLISION))
        assertNull(TeamListeningFailurePolicy.resolve(TeamActionError.INVALID_INPUT))
        assertNull(TeamListeningFailurePolicy.resolve(TeamActionError.UNKNOWN))
    }

    private fun assertPolicy(
        error: TeamActionError,
        expectedResId: Int,
        expectedReason: String,
    ) {
        val actual = TeamListeningFailurePolicy.resolve(error)
        requireNotNull(actual)
        assertEquals(expectedResId, actual.userMessageResId)
        assertEquals(expectedReason, actual.logReason)
    }
}
