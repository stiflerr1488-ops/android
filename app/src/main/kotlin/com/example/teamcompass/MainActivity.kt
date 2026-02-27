package com.example.teamcompass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airsoft.social.app.AirsoftShellLaunchControl
import com.airsoft.social.app.AirsoftRootApp
import com.airsoft.social.app.LegacyTacticalBridgeLauncherAdapter
import com.airsoft.social.app.LegacyTacticalOverviewBridge
import com.example.teamcompass.BuildConfig
import com.example.teamcompass.ui.TeamCompassApp
import com.example.teamcompass.ui.theme.TeamCompassTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var legacyTacticalOverviewBridge: LegacyTacticalOverviewBridge

    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ system splash with smooth fade-out
        val splash = installSplashScreen()
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .setDuration(350)
                .withEndAction { provider.remove() }
                .start()
        }

        super.onCreate(savedInstanceState)
        enterFullscreen()
        val useNewShell = AirsoftShellLaunchControl.shouldUseNewShell(
            launchIntent = intent,
            defaultEnabled = BuildConfig.NEW_APP_SHELL_ENABLED,
            debugOverridesEnabled = BuildConfig.DEBUG,
        )
        setContent {
            if (useNewShell) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(AirsoftShellLaunchControl.TAG_NEW_ROOT),
                ) {
                    AirsoftRootApp(
                        tacticalOverviewPort = legacyTacticalOverviewBridge,
                        tacticalLegacyBridgeLauncher = LegacyTacticalBridgeLauncherAdapter(this@MainActivity),
                    )
                }
            } else {
                TeamCompassTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(AirsoftShellLaunchControl.TAG_LEGACY_ROOT),
                    ) {
                        TeamCompassApp(
                            onLegacyTacticalOverview = legacyTacticalOverviewBridge::onLegacyShellStateChanged,
                        )
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterFullscreen()
    }
}
