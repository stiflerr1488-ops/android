plugins {
    application
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.teamcompass.AppKt")
}

dependencies {
    implementation(project(":core"))
}
