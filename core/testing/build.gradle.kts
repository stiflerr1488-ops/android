plugins {
    id("airsoft.jvm.library")
}
dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.junit4)
    testImplementation(libs.junit4)
}
