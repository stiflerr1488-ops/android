plugins {
    id("airsoft.android.library")
    id("airsoft.android.compose")
}
android {
    namespace = "com.airsoft.social.core.ui"
}
dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit4)
}
