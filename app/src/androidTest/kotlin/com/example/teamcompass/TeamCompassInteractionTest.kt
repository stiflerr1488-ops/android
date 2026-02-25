package com.example.teamcompass

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.teamcompass.ui.UserPrefs
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class TeamCompassInteractionTest {

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
    fun joinScreen_inputValidationAndButtonsWork() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Alpha_1")

        composeRule.onNodeWithTag("create_team_button", useUnmergedTree = true)
            .assertIsEnabled()

        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("123")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("123456")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsEnabled()
    }

    @Test
    fun joinScreen_createButton_toggles_withCallsignValidity() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("create_team_button", useUnmergedTree = true)
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Al")
        composeRule.onNodeWithTag("create_team_button", useUnmergedTree = true)
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Alpha_1")
        composeRule.onNodeWithTag("create_team_button", useUnmergedTree = true)
            .assertIsEnabled()
    }

    @Test
    fun joinScreen_joinButton_requiresValidCallsignAndCode() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("A")
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("123456")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Bravo_2")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsEnabled()

        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("12345")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsNotEnabled()
    }

    @Test
    fun joinScreen_joinButton_disablesAfterCodeCleared() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Charlie_3")
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("654321")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsEnabled()

        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsNotEnabled()
    }

    @Test
    fun joinScreen_joinButton_disablesWhenCallsignCleared() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Delta_4")
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("111222")
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsEnabled()

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsNotEnabled()
    }

    @Test
    fun joinScreen_joinButton_enables_forBoundaryValidInput() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Abc")

        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("000000")

        composeRule.onNodeWithTag("join_team_button", useUnmergedTree = true)
            .assertIsEnabled()
    }

    private fun waitForTag(tag: String, timeoutMs: Long) {
        composeRule.waitUntil(timeoutMs) {
            exists(tag)
        }
    }

    private fun exists(tag: String): Boolean {
        return composeRule
            .onAllNodesWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }
}
