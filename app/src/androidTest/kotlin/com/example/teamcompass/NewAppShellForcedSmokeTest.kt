package com.example.teamcompass

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airsoft.social.app.AirsoftShellLaunchControl
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewAppShellForcedSmokeTest {

    private val forceNewShellBeforeLaunch: Unit =
        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(
            AirsoftShellLaunchControl.MODE_NEW,
        )

    @Suppress("unused")
    private val keepInitializerOrder = forceNewShellBeforeLaunch

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @After
    fun clearOverride() {
        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(null)
    }

    @Test
    fun forcedNewShell_launchesNavigatesAndBridgesBackToLegacyShell() {
        waitForTag(AirsoftShellLaunchControl.TAG_NEW_ROOT)
        val rootNodes = composeRule.onAllNodesWithTag(
            AirsoftShellLaunchControl.TAG_NEW_ROOT,
            useUnmergedTree = true,
        ).fetchSemanticsNodes()
        assertTrue("Expected new shell root tag", rootNodes.isNotEmpty())

        waitForText("Onboarding")
        composeRule.onNodeWithText("Continue").performClick()

        waitForText("Auth")
        composeRule.onNodeWithText("Continue as guest").performClick()

        waitForText("Chats")
        assertTextExists("Teams")
        assertTextExists("Events")
        assertTextExists("Market")
        assertTextExists("Profile")

        composeRule.onNodeWithText("Menu").performClick()
        waitForText("V BOI!")
        composeRule.onNodeWithText("V BOI!").performClick()
        waitForText("Radar")
        assertTextExists("Tactical")
        composeRule.onNodeWithText("V BOI!").performClick()

        waitForTag(AirsoftShellLaunchControl.TAG_LEGACY_ROOT)
        val legacyRootNodes = composeRule.onAllNodesWithTag(
            AirsoftShellLaunchControl.TAG_LEGACY_ROOT,
            useUnmergedTree = true,
        ).fetchSemanticsNodes()
        assertTrue("Expected legacy shell root tag after tactical bridge relaunch", legacyRootNodes.isNotEmpty())
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(20_000L) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(20_000L) {
            composeRule.onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertTextExists(text: String) {
        val nodes = composeRule.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Expected text '$text' to exist", nodes.isNotEmpty())
    }
}
