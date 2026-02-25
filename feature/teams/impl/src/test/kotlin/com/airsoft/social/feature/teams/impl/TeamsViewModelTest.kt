package com.airsoft.social.feature.teams.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class TeamsViewModelTest {

    @Test
    fun `select recruiting filter updates state`() {
        val viewModel = TeamsViewModel()

        viewModel.onAction(TeamsAction.SelectRecruitingFilter("Medic"))

        assertEquals("Medic", viewModel.uiState.value.selectedRecruitingFilter)
    }
}

