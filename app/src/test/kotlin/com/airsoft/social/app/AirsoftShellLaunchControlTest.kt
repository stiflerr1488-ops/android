package com.airsoft.social.app

import android.content.Intent
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AirsoftShellLaunchControlTest {

    @After
    fun cleanup() {
        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(null)
    }

    @Test
    fun defaultMode_fallsBackToBuildConfigFlag() {
        assertTrue(
            AirsoftShellLaunchControl.shouldUseNewShell(
                launchIntent = null,
                defaultEnabled = true,
                debugOverridesEnabled = true,
            ),
        )
        assertFalse(
            AirsoftShellLaunchControl.shouldUseNewShell(
                launchIntent = null,
                defaultEnabled = false,
                debugOverridesEnabled = true,
            ),
        )
    }

    @Test
    fun processOverride_isUsedInDebug() {
        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(AirsoftShellLaunchControl.MODE_NEW)
        assertTrue(
            AirsoftShellLaunchControl.shouldUseNewShell(
                launchIntent = null,
                defaultEnabled = false,
                debugOverridesEnabled = true,
            ),
        )

        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(AirsoftShellLaunchControl.MODE_LEGACY)
        assertFalse(
            AirsoftShellLaunchControl.shouldUseNewShell(
                launchIntent = null,
                defaultEnabled = true,
                debugOverridesEnabled = true,
            ),
        )
    }

    @Test
    fun intentOverride_hasPriorityOverProcessOverride() {
        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(AirsoftShellLaunchControl.MODE_NEW)
        val legacyIntent = Intent().putExtra(
            AirsoftShellLaunchControl.EXTRA_SHELL_MODE,
            AirsoftShellLaunchControl.MODE_LEGACY,
        )

        assertFalse(
            AirsoftShellLaunchControl.shouldUseNewShell(
                launchIntent = legacyIntent,
                defaultEnabled = true,
                debugOverridesEnabled = true,
            ),
        )
    }

    @Test
    fun intentOverride_stillWorksWhenDebugOverridesDisabled() {
        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(AirsoftShellLaunchControl.MODE_NEW)
        val newIntent = Intent().putExtra(
            AirsoftShellLaunchControl.EXTRA_SHELL_MODE,
            AirsoftShellLaunchControl.MODE_NEW,
        )

        assertTrue(
            AirsoftShellLaunchControl.shouldUseNewShell(
                launchIntent = newIntent,
                defaultEnabled = false,
                debugOverridesEnabled = false,
            ),
        )
    }

    @Test
    fun processOverride_isIgnoredWhenDebugOverridesDisabled() {
        AirsoftShellLaunchControl.setDebugProcessOverrideModeForTests(AirsoftShellLaunchControl.MODE_NEW)

        assertFalse(
            AirsoftShellLaunchControl.shouldUseNewShell(
                launchIntent = null,
                defaultEnabled = false,
                debugOverridesEnabled = false,
            ),
        )
    }
}
