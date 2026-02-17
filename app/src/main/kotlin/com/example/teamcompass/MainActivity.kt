package com.example.teamcompass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.teamcompass.ui.TeamCompassApp
import com.example.teamcompass.ui.theme.TeamCompassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TeamCompassTheme(darkTheme = true) {
                TeamCompassApp()
            }
        }
    }
}
