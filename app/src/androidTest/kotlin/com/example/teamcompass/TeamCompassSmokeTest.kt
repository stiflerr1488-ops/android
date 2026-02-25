package com.example.teamcompass

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import android.view.ViewGroup
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeamCompassSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_andShowsAppRoot() {
        composeRule.waitUntil(20_000L) {
            val content = composeRule.activity.findViewById<ViewGroup>(android.R.id.content)
            content != null && content.childCount > 0
        }
        val content = composeRule.activity.findViewById<ViewGroup>(android.R.id.content)
        assertTrue("Activity content view is empty", content != null && content.childCount > 0)
    }
}
