package com.airsoft.social.feature.profile.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileViewModelTest {

    @Test
    fun `select role tag updates state`() {
        val viewModel = ProfileViewModel()

        viewModel.onAction(ProfileAction.SelectRoleTag("CQB"))

        assertEquals("CQB", viewModel.uiState.value.selectedRoleTag)
    }
}

