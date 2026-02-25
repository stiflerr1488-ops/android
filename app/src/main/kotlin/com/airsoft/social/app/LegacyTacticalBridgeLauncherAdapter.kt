package com.airsoft.social.app

import android.content.Intent
import com.airsoft.social.feature.tactical.api.TacticalLegacyBridgeLauncher
import com.example.teamcompass.MainActivity

class LegacyTacticalBridgeLauncherAdapter(
    private val activity: MainActivity,
) : TacticalLegacyBridgeLauncher {
    override fun openLegacyTactical() {
        val relaunchIntent = Intent(activity.intent).apply {
            putExtra(
                AirsoftShellLaunchControl.EXTRA_SHELL_MODE,
                AirsoftShellLaunchControl.MODE_LEGACY,
            )
        }
        activity.setIntent(relaunchIntent)
        activity.recreate()
    }
}
