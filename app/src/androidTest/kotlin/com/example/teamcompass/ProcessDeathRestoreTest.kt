package com.example.teamcompass

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.teamcompass.ui.UserPrefs
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProcessDeathRestoreTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val appContext = ApplicationProvider.getApplicationContext<Application>()
    private val prefs = UserPrefs(appContext)

    @Before
    fun resetAppState() {
        runBlocking {
            prefs.setTeamCode(null)
            prefs.setCallsign("")
            prefs.setShowOnboardingOnce(false)
            prefs.setShowCompassHelpOnce(false)
        }
    }

    @Test
    fun activityRecreate_restoresJoinDraftState() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Restore_One")

        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("123456")

        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsEnabled()

        composeRule.activityRule.scenario.recreate()
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .assertTextContains("Restore_One")
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .assertTextContains("123456")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsEnabled()
    }

    private fun waitForTag(tag: String, timeoutMs: Long) {
        composeRule.waitUntil(timeoutMs) {
            composeRule
                .onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
