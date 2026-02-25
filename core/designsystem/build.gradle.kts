plugins {
    id("airsoft.android.library")
    id("airsoft.android.compose")
}
android {
    namespace = "com.airsoft.social.core.designsystem"
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.core.ktx)
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation(libs.junit4)
}
