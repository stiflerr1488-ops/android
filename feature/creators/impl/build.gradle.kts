plugins {
    id("airsoft.android.feature")
}

android {
    namespace = "com.airsoft.social.feature.creators.impl"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":feature:creators:api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit4)
}
