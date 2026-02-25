package com.airsoft.social.app

import android.content.Intent

object AirsoftShellLaunchControl {
    const val EXTRA_SHELL_MODE: String = "com.airsoft.social.extra.SHELL_MODE"
    const val MODE_DEFAULT: String = "default"
    const val MODE_LEGACY: String = "legacy"
    const val MODE_NEW: String = "new"

    const val TAG_LEGACY_ROOT: String = "legacy_shell_root"
    const val TAG_NEW_ROOT: String = "new_shell_root"

    @Volatile
    private var debugProcessOverrideMode: String? = null

    fun setDebugProcessOverrideModeForTests(mode: String?) {
        debugProcessOverrideMode = mode?.trim()?.lowercase()
    }

    fun shouldUseNewShell(
        launchIntent: Intent?,
        defaultEnabled: Boolean,
        debugOverridesEnabled: Boolean,
    ): Boolean {
        if (!debugOverridesEnabled) return defaultEnabled

        val intentRequestedMode = launchIntent
            ?.getStringExtra(EXTRA_SHELL_MODE)
            ?.trim()
            ?.lowercase()
        val requestedMode = intentRequestedMode ?: debugProcessOverrideMode ?: MODE_DEFAULT

        return when (requestedMode) {
            MODE_NEW -> true
            MODE_LEGACY -> false
            else -> defaultEnabled
        }
    }
}
