plugins {
    id("airsoft.android.feature")
}

android {
    namespace = "com.airsoft.social.feature.calendarsync.impl"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":feature:calendarsync:api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit4)
}
