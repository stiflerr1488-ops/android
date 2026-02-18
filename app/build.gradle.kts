plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.teamcompass"
    compileSdk = 34

    val rtdbUrl = (project.findProperty("TEAMCOMPASS_RTDB_URL") as String?)?.trim().orEmpty()

    defaultConfig {
        applicationId = "com.example.teamcompass"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.2"
        buildConfigField("String", "RTDB_URL", "\"${rtdbUrl.replace("\"", "\\\"")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Align Java/Kotlin bytecode targets (avoids "Inconsistent JVM-target" build failures).
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Kotlin 2 + Compose plugin: compiler is wired automatically.
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))

    // AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    // Filled icons like Tune/GpsFixed/Groups/ContentCopy/SwapHoriz live here.
    implementation("androidx.compose.material:material-icons-extended")
    // Needed for XML themes (Theme.MaterialComponents.*) used by the Activity.
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // Location + Sensors
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // DataStore (callsign persistence)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Firebase (Auth + Realtime DB)
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
