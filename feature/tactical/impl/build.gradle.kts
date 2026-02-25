plugins {
    id("airsoft.android.feature")
}

android {
    namespace = "com.airsoft.social.feature.tactical.impl"
}

dependencies {
    implementation(project(":feature:tactical:api"))
    implementation(project(":core:tactical"))
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.junit4)
}
