@file:Suppress("UnstableApiUsage")

import dev.mattramotar.updatingitem.tooling.extensions.android

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id("plugin.updatingitem.android.library")
    id("plugin.updatingitem.kotlin.multiplatform")
}

android {
    namespace = "dev.mattramotar.updatingitem.runtime"

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(compose.runtime)
                implementation(libs.molecule.runtime)
            }
        }
    }
}