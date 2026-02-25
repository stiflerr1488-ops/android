package com.example.teamcompass.ui.theme

import androidx.compose.ui.graphics.Color

object StatusColors {
    // Success states
    val success = Color(0xFF22C55E)
    val onSuccess = Color.White
    
    // Warning states
    val warning = Color(0xFFF59E0B)
    val onWarning = Color(0xFF10120B)
    
    // Error states
    val error = Color(0xFFEF4444)
    val onError = Color.White
    
    // Info states
    val info = Color(0xFF3B82F6)
    val onInfo = Color.White
    
    // Dead mode (transparent red)
    val dead = Color(0xFFFF4D4D).copy(alpha = 0.65f)
    
    // Anchored mode (blue)
    val anchored = Color(0xFF3B82F6)
    
    // Game mode (green)
    val game = Color(0xFF22C55E)
}
