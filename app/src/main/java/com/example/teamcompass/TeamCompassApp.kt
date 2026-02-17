package com.example.teamcompass

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class TeamCompassApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Safe to call; returns null only if google-services.json is missing.
        FirebaseApp.initializeApp(this)

        // Optional but very useful for "лес с перебоями": кеширует RTDB на устройстве.
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Ignore: can throw if called more than once.
        }
    }
}
