plugins {
    id("airsoft.android.feature")
}
android {
    namespace = "com.airsoft.social.feature.chats.impl"
}
dependencies {
    implementation(project(":feature:chats:api"))
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit4)
}
