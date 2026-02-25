import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.findByName("android")?.let { extension ->
    when (extension) {
        is ApplicationExtension -> extension.buildFeatures.compose = true
        is LibraryExtension -> extension.buildFeatures.compose = true
    }
}

