package com.example.teamcompass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.teamcompass.ui.TeamCompassApp
import com.example.teamcompass.ui.theme.TeamCompassTheme

class MainActivity : ComponentActivity() {
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
        setContent {
            TeamCompassTheme(darkTheme = true) {
                TeamCompassApp()
            }
        }
    }
}
