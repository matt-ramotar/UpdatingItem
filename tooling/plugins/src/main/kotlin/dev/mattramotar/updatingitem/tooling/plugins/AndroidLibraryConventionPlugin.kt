package dev.mattramotar.updatingitem.tooling.plugins

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import dev.mattramotar.updatingitem.tooling.extensions.Versions
import dev.mattramotar.updatingitem.tooling.extensions.configureAndroid
import dev.mattramotar.updatingitem.tooling.extensions.configureFlavors

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                configureAndroid()
                configureFlavors(this)
                defaultConfig.targetSdk = Versions.TARGET_SDK
            }
        }
    }
}
