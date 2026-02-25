package com.example.teamcompass.data.firebase

import com.google.firebase.database.DatabaseError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseTeamRepositoryStateCellsFallbackTest {

    @Test
    fun fallback_enabled_for_permission_denied_on_state_cell_listener() {
        assertTrue(
            shouldFallbackToLegacyStateListener(
                listenerKey = "stateCell:udts4z",
                errorCode = DatabaseError.PERMISSION_DENIED,
                stateCellsReadEnabled = true,
            )
        )
    }

    @Test
    fun fallback_disabled_for_non_state_cell_listener() {
        assertFalse(
            shouldFallbackToLegacyStateListener(
                listenerKey = "state",
                errorCode = DatabaseError.PERMISSION_DENIED,
                stateCellsReadEnabled = true,
            )
        )
    }

    @Test
    fun fallback_disabled_for_non_permission_error() {
        assertFalse(
            shouldFallbackToLegacyStateListener(
                listenerKey = "stateCell:udts4z",
                errorCode = DatabaseError.NETWORK_ERROR,
                stateCellsReadEnabled = true,
            )
        )
    }

    @Test
    fun fallback_disabled_when_state_cells_already_disabled() {
        assertFalse(
            shouldFallbackToLegacyStateListener(
                listenerKey = "stateCell:udts4z",
                errorCode = DatabaseError.PERMISSION_DENIED,
                stateCellsReadEnabled = false,
            )
        )
    }

    @Test
    fun state_cell_permission_denied_detected_even_without_fallback_gate() {
        assertTrue(
            isStateCellPermissionDenied(
                listenerKey = "stateCell:udts4z",
                errorCode = DatabaseError.PERMISSION_DENIED,
            )
        )
    }

    @Test
    fun state_cell_permission_denied_not_detected_for_other_errors() {
        assertFalse(
            isStateCellPermissionDenied(
                listenerKey = "stateCell:udts4z",
                errorCode = DatabaseError.NETWORK_ERROR,
            )
        )
    }

    @Test
    fun should_enable_state_cells_read_when_feature_enabled_and_not_denied_in_session() {
        assertTrue(
            shouldEnableStateCellsRead(
                stateCellsEnabled = true,
                stateCellsReadDeniedInSession = false,
            )
        )
    }

    @Test
    fun should_disable_state_cells_read_when_denied_in_session() {
        assertFalse(
            shouldEnableStateCellsRead(
                stateCellsEnabled = true,
                stateCellsReadDeniedInSession = true,
            )
        )
    }

    @Test
    fun permission_denied_message_detected_case_insensitive() {
        assertTrue(isPermissionDeniedMessage("Permission denied"))
        assertTrue(isPermissionDeniedMessage("PERMISSION DENIED while reading path"))
    }

    @Test
    fun permission_denied_message_not_detected_for_other_messages() {
        assertFalse(isPermissionDeniedMessage("network timeout"))
    }

    @Test
    fun state_cells_probe_permission_denied_detected_from_throwable_message() {
        assertTrue(
            isStateCellsProbePermissionDenied(
                RuntimeException("DatabaseError: Permission denied"),
            )
        )
    }

    @Test
    fun state_cells_probe_permission_denied_not_detected_for_unrelated_error() {
        assertFalse(
            isStateCellsProbePermissionDenied(
                RuntimeException("DatabaseError: Network error"),
            )
        )
    }

    @Test
    fun preflight_fallback_enabled_on_timeout() {
        assertTrue(
            shouldFallbackAfterStateCellsPreflight(
                timedOut = true,
                failure = null,
            )
        )
    }

    @Test
    fun preflight_fallback_enabled_on_probe_failure() {
        assertTrue(
            shouldFallbackAfterStateCellsPreflight(
                timedOut = false,
                failure = RuntimeException("probe failed"),
            )
        )
    }

    @Test
    fun preflight_fallback_disabled_on_successful_probe() {
        assertFalse(
            shouldFallbackAfterStateCellsPreflight(
                timedOut = false,
                failure = null,
            )
        )
    }
}
