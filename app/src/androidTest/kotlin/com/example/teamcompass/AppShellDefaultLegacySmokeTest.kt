package com.example.teamcompass

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airsoft.social.app.AirsoftShellLaunchControl
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppShellDefaultLegacySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_usesLegacyShellByDefault_whenNewShellFlagIsDisabled() {
        waitForTag(AirsoftShellLaunchControl.TAG_LEGACY_ROOT)
        val nodes = composeRule.onAllNodesWithTag(
            AirsoftShellLaunchControl.TAG_LEGACY_ROOT,
            useUnmergedTree = true,
        ).fetchSemanticsNodes()
        assertTrue("Expected legacy shell root tag", nodes.isNotEmpty())
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(20_000L) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
