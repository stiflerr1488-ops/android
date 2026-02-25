package com.example.teamcompass.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerCorePoliciesTest {

    @Test
    fun canEdit_and_canDelete_are_author_only_for_team_markers() {
        val marker = marker(scope = MarkerScope.TEAM, ownerUid = "author")

        assertTrue(MarkerCorePolicies.canEdit(marker, "author"))
        assertTrue(MarkerCorePolicies.canDelete(marker, "author"))
        assertFalse(MarkerCorePolicies.canEdit(marker, "other"))
        assertFalse(MarkerCorePolicies.canDelete(marker, "other"))
    }

    @Test
    fun canEdit_private_marker_without_owner_is_backward_compatible() {
        val marker = marker(scope = MarkerScope.PRIVATE, ownerUid = null)
        assertTrue(MarkerCorePolicies.canEdit(marker, "anyone"))
    }

    @Test
    fun isVisible_hides_private_marker_from_other_users() {
        val privateMarker = marker(scope = MarkerScope.PRIVATE, ownerUid = "a")
        val teamMarker = marker(scope = MarkerScope.TEAM, ownerUid = "a")

        assertTrue(MarkerCorePolicies.isVisible(privateMarker, "a"))
        assertFalse(MarkerCorePolicies.isVisible(privateMarker, "b"))
        assertTrue(MarkerCorePolicies.isVisible(teamMarker, "b"))
    }

    @Test
    fun resolveState_marks_expired_and_preserves_disabled() {
        val now = 1_700_000_000_000L
        val expired = marker(state = MarkerState.ACTIVE, expiresAtMs = now - 1L)
        val disabled = marker(state = MarkerState.DISABLED, expiresAtMs = now + 60_000L)

        assertEquals(MarkerState.EXPIRED, MarkerCorePolicies.resolveState(expired, now))
        assertEquals(MarkerState.DISABLED, MarkerCorePolicies.resolveState(disabled, now))
    }

    @Test
    fun isDuplicate_respects_distance_icon_and_label() {
        val baseLat = 55.0
        val baseLon = 37.0
        val near = destinationPoint(baseLat, baseLon, 90.0, 2.5)
        val far = destinationPoint(baseLat, baseLon, 90.0, 2.6)

        val same = MarkerCorePolicies.isDuplicate(
            latA = baseLat,
            lonA = baseLon,
            labelA = "Alpha",
            iconRawA = TacticalIconId.FLAG.raw,
            latB = near.first,
            lonB = near.second,
            labelB = "alpha",
            iconRawB = TacticalIconId.FLAG.raw,
            toleranceM = 2.5,
        )
        val tooFar = MarkerCorePolicies.isDuplicate(
            latA = baseLat,
            lonA = baseLon,
            labelA = "Alpha",
            iconRawA = TacticalIconId.FLAG.raw,
            latB = far.first,
            lonB = far.second,
            labelB = "Alpha",
            iconRawB = TacticalIconId.FLAG.raw,
            toleranceM = 2.5,
        )
        val otherIcon = MarkerCorePolicies.isDuplicate(
            latA = baseLat,
            lonA = baseLon,
            labelA = "Alpha",
            iconRawA = TacticalIconId.FLAG.raw,
            latB = near.first,
            lonB = near.second,
            labelB = "Alpha",
            iconRawB = TacticalIconId.OBJECTIVE.raw,
            toleranceM = 2.5,
        )

        assertTrue(same)
        assertFalse(tooFar)
        assertFalse(otherIcon)
    }

    private fun marker(
        scope: MarkerScope = MarkerScope.TEAM,
        ownerUid: String? = "author",
        state: MarkerState = MarkerState.ACTIVE,
        expiresAtMs: Long = 0L,
    ): UnifiedMarker {
        return UnifiedMarker(
            id = "m1",
            kind = MarkerKind.POINT,
            scope = scope,
            state = state,
            ownerUid = ownerUid,
            lat = 55.0,
            lon = 37.0,
            label = "A",
            iconRaw = TacticalIconId.FLAG.raw,
            colorArgb = null,
            createdAtMs = 10L,
            updatedAtMs = 10L,
            expiresAtMs = expiresAtMs,
            source = MarkerSource.TEAM_POINT,
        )
    }
}
