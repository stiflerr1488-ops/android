package com.example.teamcompass

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.teamcompass.ui.SettingsScreen
import com.example.teamcompass.ui.UiState
import com.example.teamcompass.ui.theme.TeamCompassTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilitySmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsBackButton_hasContentDescription_andIsClickable() {
        var backPressed = false
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        composeRule.setContent {
            TeamCompassTheme {
                SettingsScreen(
                    state = UiState(),
                    onBack = { backPressed = true },
                    onDefaultMode = {},
                    onGamePolicy = { _, _ -> },
                    onSilentPolicy = { _, _ -> },
                    onControlLayoutEdit = {},
                    onResetControlPositions = {},
                )
            }
        }

        val backLabel = context.getString(R.string.nav_back)
        val nodes = composeRule.onAllNodesWithContentDescription(backLabel, useUnmergedTree = true)
            .fetchSemanticsNodes()
        assertTrue("Back button node with contentDescription '$backLabel' not found", nodes.isNotEmpty())
        composeRule.onNodeWithContentDescription(backLabel, useUnmergedTree = true).performClick()
        assertTrue("Back callback was not invoked", backPressed)
    }
}
