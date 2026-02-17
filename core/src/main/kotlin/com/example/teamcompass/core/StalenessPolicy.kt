package com.example.teamcompass.core

object StalenessPolicy {
    fun classify(ageSec: Long): Staleness = when {
        ageSec <= 20 -> Staleness.FRESH
        ageSec <= 60 -> Staleness.SUSPECT
        ageSec <= 120 -> Staleness.STALE
        else -> Staleness.HIDDEN
    }
}
