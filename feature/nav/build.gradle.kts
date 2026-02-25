plugins {
    id("airsoft.android.feature")
}
android {
    namespace = "com.airsoft.social.feature.nav"
}
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:tactical"))
    implementation(project(":core:ui"))
    implementation(project(":feature:onboarding:api"))
    implementation(project(":feature:onboarding:impl"))
    implementation(project(":feature:auth:api"))
    implementation(project(":feature:auth:impl"))
    implementation(project(":feature:chats:api"))
    implementation(project(":feature:chats:impl"))
    implementation(project(":feature:teams:api"))
    implementation(project(":feature:teams:impl"))
    implementation(project(":feature:events:api"))
    implementation(project(":feature:events:impl"))
    implementation(project(":feature:marketplace:api"))
    implementation(project(":feature:marketplace:impl"))
    implementation(project(":feature:profile:api"))
    implementation(project(":feature:profile:impl"))
    implementation(project(":feature:tactical:api"))
    implementation(project(":feature:tactical:impl"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit4)
}
