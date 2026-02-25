package com.example.teamcompass.tracking

import android.content.Intent
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TrackingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrackingCommandsTest {

    @Test
    fun parseStartConfig_returnsNull_when_action_is_not_start() {
        val intent = Intent().apply { action = "other_action" }

        val parsed = TrackingCommands.parseStartConfig(intent)

        assertNull(parsed)
    }

    @Test
    fun parseStartConfig_returnsNull_when_required_fields_missing() {
        val intent = Intent().apply {
            action = TrackingCommands.ACTION_START
            putExtra("extra_team_code", "")
            putExtra("extra_uid", "")
        }

        val parsed = TrackingCommands.parseStartConfig(intent)

        assertNull(parsed)
    }

    @Test
    fun parseStartConfig_invalid_mode_values_fallback_to_game_defaults() {
        val intent = Intent().apply {
            action = TrackingCommands.ACTION_START
            putExtra("extra_team_code", "123456")
            putExtra("extra_uid", "u-1")
            putExtra("extra_callsign", "Alpha")
            putExtra("extra_mode", "BROKEN_MODE")
            putExtra("extra_player_mode", "BROKEN_PLAYER_MODE")
        }

        val parsed = TrackingCommands.parseStartConfig(intent)

        assertNotNull(parsed)
        val config = requireNotNull(parsed)
        assertEquals(TrackingMode.GAME, config.mode)
        assertEquals(PlayerMode.GAME, config.playerMode)
    }

    @Test
    fun parseStartConfig_policy_values_are_clamped_to_minimums() {
        val intent = Intent().apply {
            action = TrackingCommands.ACTION_START
            putExtra("extra_team_code", "654321")
            putExtra("extra_uid", "u-2")
            putExtra("extra_callsign", "Bravo")
            putExtra("extra_game_interval_ms", 10L)
            putExtra("extra_game_distance_m", 0.2)
            putExtra("extra_silent_interval_ms", 100L)
            putExtra("extra_silent_distance_m", 0.5)
        }

        val parsed = TrackingCommands.parseStartConfig(intent)

        assertNotNull(parsed)
        val config = requireNotNull(parsed)
        assertEquals(1_000L, config.gamePolicy.minIntervalMs)
        assertEquals(1.0, config.gamePolicy.minDistanceMeters, 0.0001)
        assertEquals(1_000L, config.silentPolicy.minIntervalMs)
        assertEquals(1.0, config.silentPolicy.minDistanceMeters, 0.0001)
    }
}
