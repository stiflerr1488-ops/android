plugins {
    id("airsoft.jvm.library")
}
dependencies {
    implementation(project(":core:model"))
    testImplementation(libs.junit4)
}
