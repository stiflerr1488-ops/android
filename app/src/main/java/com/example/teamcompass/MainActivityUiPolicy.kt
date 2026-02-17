package com.example.teamcompass

import com.example.teamcompass.core.Staleness
import com.example.teamcompass.core.StalenessPolicy

object MainActivityUiPolicy {
    fun isHidden(ageSec: Long): Boolean =
        StalenessPolicy.classify(ageSec) == Staleness.HIDDEN

    fun staleMark(ageSec: Long): String = when (StalenessPolicy.classify(ageSec)) {
        Staleness.FRESH -> ""
        Staleness.SUSPECT -> " (сомн)"
        Staleness.STALE -> " (старые)"
        Staleness.HIDDEN -> ""
    }
}
