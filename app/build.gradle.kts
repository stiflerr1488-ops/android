import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.teamcompass"
    compileSdk = 36

    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(::load)
        }
    }
    val rtdbUrl = sequenceOf(
        project.findProperty("TEAMCOMPASS_RTDB_URL") as String?,
        System.getenv("TEAMCOMPASS_RTDB_URL"),
        localProperties.getProperty("TEAMCOMPASS_RTDB_URL"),
    ).map { it?.trim().orEmpty() }.firstOrNull { it.isNotEmpty() }.orEmpty()

    defaultConfig {
        applicationId = "com.example.teamcompass"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.2"
        buildConfigField("String", "RTDB_URL", "\"${rtdbUrl.replace("\"", "\\\"")}\"")
        buildConfigField("boolean", "NEW_APP_SHELL_ENABLED", "false")
        buildConfigField("boolean", "NEW_APP_USE_FIREBASE_ADAPTERS", "false")
        buildConfigField("boolean", "TACTICAL_FILTERS_V1_ENABLED", "true")
        buildConfigField("boolean", "TEAM_VIEW_MODE_V2_ENABLED", "true")
        buildConfigField("boolean", "STATE_CELLS_V1_ENABLED", "true")
        buildConfigField("boolean", "P2P_BRIDGES_ENABLED", "false")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val androidTestClass = (project.findProperty("androidTestClass") as String?)?.trim().orEmpty()
        if (androidTestClass.isNotBlank()) {
            testInstrumentationRunnerArguments["class"] = androidTestClass
        }
        val androidTestArgs = (project.findProperty("androidTestArgs") as String?)?.trim().orEmpty()
        if (androidTestArgs.isNotBlank()) {
            androidTestArgs
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { entry ->
                    val separator = entry.indexOf('=')
                    if (separator <= 0 || separator == entry.length - 1) {
                        throw org.gradle.api.GradleException(
                            "Invalid -PandroidTestArgs entry '$entry'. Expected key=value.",
                        )
                    }
                    val key = entry.substring(0, separator).trim()
                    val value = entry.substring(separator + 1).trim()
                    if (key.isEmpty() || value.isEmpty()) {
                        throw org.gradle.api.GradleException(
                            "Invalid -PandroidTestArgs entry '$entry'. Expected non-empty key/value.",
                        )
                    }
                    testInstrumentationRunnerArguments[key] = value
                }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // Align Java/Kotlin bytecode targets (avoids "Inconsistent JVM-target" build failures).
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        disable += "PropertyEscape"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:model"))
    implementation(project(":core:auth"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:realtime"))
    implementation(project(":core:telemetry"))
    implementation(project(":core:tactical"))
    implementation(project(":feature:nav"))
    implementation(project(":feature:tactical:api"))
    implementation(project(":infra:firebase"))
    // Recovery jar disabled after restoring TeamCompassViewModel as source (Java).
    // implementation(files("libs/recovered-teamcompass-vm.jar"))

    // AndroidX
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation(platform("androidx.compose:compose-bom:2026.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    // Filled icons like Tune/GpsFixed/Groups/ContentCopy/SwapHoriz live here.
    implementation("androidx.compose.material:material-icons-extended")
    // Needed for XML themes (Theme.MaterialComponents.*) used by the Activity.
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // Location + Sensors
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // DataStore (callsign persistence)
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // QR codes (ZXing for generation and scanning)
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Firebase (Auth + Realtime DB + Crashlytics + Analytics)
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("org.robolectric:robolectric:4.16.1")

    androidTestImplementation(platform("androidx.compose:compose-bom:2026.02.00"))
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}
