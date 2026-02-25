package com.example.teamcompass.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventNotificationManagerTest {

    @Test
    fun shouldSendSosNotification_throttles_same_player_in_suppression_window() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = EventNotificationManager(app)
        val now = 1_000_000L
        val player = sosPlayer(uid = "u-1", nick = "Alpha", nowMs = now, sosUntilMs = now + 60_000L)

        assertTrue(manager.shouldSendSosNotification(player, now))
        assertFalse(manager.shouldSendSosNotification(player, now + 5_000L))
        assertTrue(manager.shouldSendSosNotification(player, now + 16_000L))
    }

    @Test
    fun shouldSendSosNotification_allows_different_players_without_blocking_each_other() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = EventNotificationManager(app)
        val now = 2_000_000L
        val first = sosPlayer(uid = "u-1", nick = "Alpha", nowMs = now, sosUntilMs = now + 60_000L)
        val second = sosPlayer(uid = "u-2", nick = "Bravo", nowMs = now, sosUntilMs = now + 60_000L)

        assertTrue(manager.shouldSendSosNotification(first, now))
        assertTrue(manager.shouldSendSosNotification(second, now + 1_000L))
    }

    @Test
    fun shouldSendSosNotification_ignores_inactive_sos() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = EventNotificationManager(app)
        val now = 3_000_000L
        val player = sosPlayer(uid = "u-3", nick = "Charlie", nowMs = now, sosUntilMs = now - 1L)

        assertFalse(manager.shouldSendSosNotification(player, now))
    }

    @Test
    fun shouldSendSosNotification_when_uid_blank_uses_nick_for_suppression_key() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = EventNotificationManager(app)
        val now = 4_000_000L
        val first = sosPlayer(uid = "", nick = "Alpha", nowMs = now, sosUntilMs = now + 60_000L)
        val second = sosPlayer(uid = " ", nick = "Alpha", nowMs = now, sosUntilMs = now + 60_000L)

        assertTrue(manager.shouldSendSosNotification(first, now))
        assertFalse(manager.shouldSendSosNotification(second, now + 1_000L))
    }

    @Test
    fun sosNotificationIdForKey_is_stable_and_distinguishes_different_players() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = EventNotificationManager(app)

        val alphaA = manager.sosNotificationIdForKey("alpha")
        val alphaB = manager.sosNotificationIdForKey("alpha")
        val bravo = manager.sosNotificationIdForKey("bravo")

        assertEquals(alphaA, alphaB)
        assertNotEquals(alphaA, bravo)
        assertTrue(alphaA in 11_000 until 19_000)
        assertTrue(bravo in 11_000 until 19_000)
    }

    @Test
    fun sosNotificationIdFromHash_handles_IntMinValue_and_stays_in_range() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = EventNotificationManager(app)

        val id = manager.sosNotificationIdFromHash(Int.MIN_VALUE)

        assertTrue(id in 11_000 until 19_000)
    }

    private fun sosPlayer(uid: String, nick: String, nowMs: Long, sosUntilMs: Long): PlayerState {
        return PlayerState(
            uid = uid,
            nick = nick,
            point = LocationPoint(
                lat = 55.0,
                lon = 37.0,
                accMeters = 5.0,
                speedMps = 0.0,
                headingDeg = null,
                timestampMs = nowMs,
            ),
            sosUntilMs = sosUntilMs,
        )
    }
}
