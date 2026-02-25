package com.example.teamcompass.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.teamcompass.R
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TeamActionErrorPolicyTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun maps_not_found_to_friendly_message() {
        val message = TeamActionErrorPolicy.toUserMessage(
            context = context,
            defaultMessage = "fallback",
            failure = TeamActionFailure(
                error = TeamActionError.NOT_FOUND,
                message = "not found",
            ),
        )
        assertEquals(context.getString(R.string.error_team_code_not_found), message)
    }

    @Test
    fun permission_denied_with_locked_hint_maps_to_locked_message() {
        val message = TeamActionErrorPolicy.toUserMessage(
            context = context,
            defaultMessage = "fallback",
            failure = TeamActionFailure(
                error = TeamActionError.PERMISSION_DENIED,
                message = "LOCKED by server rule",
            ),
        )
        assertEquals(context.getString(R.string.error_match_locked), message)
    }

    @Test
    fun permission_denied_with_throttled_hint_maps_to_rate_limit_message() {
        val message = TeamActionErrorPolicy.toUserMessage(
            context = context,
            defaultMessage = "fallback",
            failure = TeamActionFailure(
                error = TeamActionError.PERMISSION_DENIED,
                message = "throttled_enemy_ping",
            ),
        )
        assertEquals(context.getString(R.string.error_too_many_requests), message)
    }
}

