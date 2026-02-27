plugins {
    id("airsoft.android.library")
    id("airsoft.android.hilt")
}

android {
    namespace = "com.airsoft.social.infra.device"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")
    testImplementation(libs.junit4)
}
