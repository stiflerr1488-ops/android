package com.example.teamcompass

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.teamcompass.ui.UserPrefs
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrientationAndBackNavigationTest {

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
    fun orientationChange_preservesJoinDraftState() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .performTextInput("Rotate_User")

        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextClearance()
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .performTextInput("654321")

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }

        waitForTag("join_screen", timeoutMs = 25_000L)
        composeRule.onNodeWithTag("callsign_input", useUnmergedTree = true)
            .assertTextContains("Rotate_User")
        composeRule.onNodeWithTag("team_code_input", useUnmergedTree = true)
            .assertTextContains("654321")
    }

    @Test
    fun backPress_fromJoin_finishesActivity() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.activityRule.scenario.state == Lifecycle.State.DESTROYED
        }
        assertEquals(Lifecycle.State.DESTROYED, composeRule.activityRule.scenario.state)
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

