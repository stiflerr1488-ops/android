package com.example.teamcompass

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airsoft.social.app.AirsoftShellLaunchControl
import com.airsoft.social.feature.auth.impl.AUTH_CALLSIGN_FIELD_TAG
import com.airsoft.social.feature.nav.NAV_BOTTOM_TACTICAL_CTA_BUTTON_TAG
import com.airsoft.social.feature.nav.NAV_MENU_BUTTON_TAG
import com.airsoft.social.feature.nav.NAV_TACTICAL_CTA_BUTTON_TAG
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
    private val authGuestButtonTag = "auth_guest_button"

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

        waitForTag(authGuestButtonTag)
        waitForTag(AUTH_CALLSIGN_FIELD_TAG)
        composeRule.onNodeWithTag(AUTH_CALLSIGN_FIELD_TAG, useUnmergedTree = true)
            .performTextInput("Teiwaz_")
        composeRule.onNodeWithTag(authGuestButtonTag, useUnmergedTree = true).performClick()

        val bottomTacticalNodes = composeRule.onAllNodesWithTag(
            NAV_BOTTOM_TACTICAL_CTA_BUTTON_TAG,
            useUnmergedTree = true,
        ).fetchSemanticsNodes()
        if (bottomTacticalNodes.isNotEmpty()) {
            composeRule.onNodeWithTag(
                NAV_BOTTOM_TACTICAL_CTA_BUTTON_TAG,
                useUnmergedTree = true,
            ).performClick()
        } else {
            waitForTag(NAV_MENU_BUTTON_TAG)
            composeRule.onNodeWithTag(NAV_MENU_BUTTON_TAG, useUnmergedTree = true).performClick()

            waitForTag(NAV_TACTICAL_CTA_BUTTON_TAG)
            composeRule.onNodeWithTag(NAV_TACTICAL_CTA_BUTTON_TAG, useUnmergedTree = true).performClick()
        }

        waitForTag(AirsoftShellLaunchControl.TAG_LEGACY_ROOT)
        val legacyRootNodes = composeRule.onAllNodesWithTag(
            AirsoftShellLaunchControl.TAG_LEGACY_ROOT,
            useUnmergedTree = true,
        ).fetchSemanticsNodes()
        assertTrue("Expected legacy shell root tag after tactical CTA", legacyRootNodes.isNotEmpty())
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(20_000L) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
