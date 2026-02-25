plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    // Use the JDK that ships with modern Android Studio (often JBR 21).
    // This avoids Gradle toolchain failures on machines that don't have a separate JDK 17 installed.
    jvmToolchain(21)

    // Keep bytecode compatible with Android toolchain expectations.
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Gradle validates that Java and Kotlin compile tasks target the same JVM bytecode level.
// We use a JDK 21 toolchain to *run* the compiler, but we must emit JVM 17 bytecode so
// the module can be safely consumed from the Android app.
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
    options.release.set(17)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
