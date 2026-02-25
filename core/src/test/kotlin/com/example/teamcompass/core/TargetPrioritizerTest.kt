package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TargetPrioritizerTest {
    @Test
    fun prioritize_sortsByScoreDescending() {
        val targets = listOf(
            target(uid = "a", nick = "Alpha", distance = 80.0, staleness = Staleness.FRESH, sos = false),
            target(uid = "b", nick = "Bravo", distance = 320.0, staleness = Staleness.FRESH, sos = true),
            target(uid = "c", nick = "Charlie", distance = 50.0, staleness = Staleness.STALE, sos = false, lowAccuracy = true),
        )

        val prioritized = TargetPrioritizer.prioritize(targets)

        assertEquals(listOf("b", "a", "c"), prioritized.map { it.target.uid })
        assertTrue(prioritized[0].priorityScore > prioritized[1].priorityScore)
    }

    @Test
    fun applyFilters_respectsPresetAndFlags() {
        val prioritized = TargetPrioritizer.prioritize(
            listOf(
                target(uid = "sos", nick = "SOS", distance = 120.0, staleness = Staleness.FRESH, sos = true),
                target(uid = "near", nick = "Near", distance = 60.0, staleness = Staleness.FRESH, sos = false),
                target(uid = "dead", nick = "Dead", distance = 40.0, staleness = Staleness.FRESH, sos = false, mode = PlayerMode.DEAD),
                target(uid = "stale", nick = "Stale", distance = 70.0, staleness = Staleness.STALE, sos = false),
            )
        )

        val sosOnly = TargetPrioritizer.applyFilters(
            prioritized,
            TargetFilterState(preset = TargetFilterPreset.SOS),
        )
        assertEquals(listOf("sos"), sosOnly.map { it.target.uid })

        val nearOnly = TargetPrioritizer.applyFilters(
            prioritized,
            TargetFilterState(preset = TargetFilterPreset.NEAR, nearRadiusM = 65),
        )
        assertEquals(listOf("dead", "near"), nearOnly.map { it.target.uid }.sorted())

        val activeNoDeadNoStale = TargetPrioritizer.applyFilters(
            prioritized,
            TargetFilterState(
                preset = TargetFilterPreset.ACTIVE,
                showDead = false,
                showStale = false,
            ),
        )
        assertEquals(listOf("near", "sos"), activeNoDeadNoStale.map { it.target.uid }.sorted())
        assertFalse(activeNoDeadNoStale.any { it.target.mode == PlayerMode.DEAD })
        assertFalse(activeNoDeadNoStale.any { it.target.staleness in setOf(Staleness.STALE, Staleness.HIDDEN) })
    }

    @Test
    fun applyFilters_focusModeKeepsTopAndAlwaysSos() {
        val base = buildList {
            add(target(uid = "sos1", nick = "SOS1", distance = 300.0, staleness = Staleness.SUSPECT, sos = true))
            add(target(uid = "sos2", nick = "SOS2", distance = 300.0, staleness = Staleness.STALE, sos = true))
            repeat(12) { i ->
                add(
                    target(
                        uid = "p$i",
                        nick = "P$i",
                        distance = (50 + i * 10).toDouble(),
                        staleness = if (i < 5) Staleness.FRESH else Staleness.SUSPECT,
                        sos = false,
                    )
                )
            }
        }
        val prioritized = TargetPrioritizer.prioritize(base)

        val focused = TargetPrioritizer.applyFilters(
            prioritized,
            TargetFilterState(focusMode = true),
        )

        assertEquals(8, focused.size)
        assertTrue(focused.any { it.target.uid == "sos1" })
        assertTrue(focused.any { it.target.uid == "sos2" })
    }

    private fun target(
        uid: String,
        nick: String,
        distance: Double,
        staleness: Staleness,
        sos: Boolean,
        lowAccuracy: Boolean = false,
        mode: PlayerMode = PlayerMode.GAME,
    ): CompassTarget {
        return CompassTarget(
            uid = uid,
            nick = nick,
            distanceMeters = distance,
            relativeBearingDeg = 0.0,
            staleness = staleness,
            lowAccuracy = lowAccuracy,
            lastSeenSec = 5L,
            mode = mode,
            anchored = false,
            sosActive = sos,
        )
    }
}
