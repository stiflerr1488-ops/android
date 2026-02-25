plugins {
    id("airsoft.android.feature")
}
android {
    namespace = "com.airsoft.social.feature.auth.impl"
}
dependencies {
    implementation(project(":feature:auth:api"))
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    testImplementation(libs.junit4)
}
