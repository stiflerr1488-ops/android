plugins {
    id("airsoft.android.library")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.airsoft.social.core.database"
}
dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit4)
    testImplementation(libs.androidx.test.core)
}
