plugins {
    id("airsoft.android.library")
}
android {
    namespace = "com.airsoft.social.core.network"
}
dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    api(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp.logging)
    api(libs.squareup.retrofit)
    testImplementation(libs.junit4)
}
