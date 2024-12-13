package dev.mattramotar.updatingitem.tooling.plugins

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import dev.mattramotar.updatingitem.tooling.extensions.Versions
import dev.mattramotar.updatingitem.tooling.extensions.configureAndroidCompose
import dev.mattramotar.updatingitem.tooling.extensions.configureFlavors

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = Versions.COMPILE_SDK

                configureAndroidCompose(this)
                configureFlavors(this)
            }
        }
    }
}
