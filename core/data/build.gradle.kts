plugins {
    id("airsoft.android.library")
}
android {
    namespace = "com.airsoft.social.core.data"
}
dependencies {
    implementation(project(":core:common"))
    api(project(":core:model"))
    api(project(":core:auth"))
    implementation(project(":core:telemetry"))
    api(project(":core:datastore"))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
