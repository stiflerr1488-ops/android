package com.example.teamcompass

import android.app.Application
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.teamcompass.ui.UserPrefs
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityFlowSmokeTest {

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
    fun joinScreen_hasLabeledPrimaryActions() {
        waitForTag("join_screen", timeoutMs = 25_000L)

        assertTagExists("create_team_button")
        assertTagExists("join_team_button")
        assertTagExists("callsign_input")
        assertTagExists("team_code_input")
    }

    private fun waitForTag(tag: String, timeoutMs: Long) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun assertTagExists(tag: String) {
        val nodes = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Expected node with tag '$tag' to exist", nodes.isNotEmpty())
    }
}
