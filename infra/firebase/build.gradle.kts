plugins {
    id("airsoft.android.library")
}
android {
    namespace = "com.airsoft.social.infra.firebase"
}
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:auth"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:realtime"))
    implementation(project(":core:telemetry"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit4)
}
