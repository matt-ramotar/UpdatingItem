package dev.mattramotar.updatingitem.tooling.plugins

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import dev.mattramotar.updatingitem.tooling.extensions.BuildFlavor
import dev.mattramotar.updatingitem.tooling.extensions.BuildType
import dev.mattramotar.updatingitem.tooling.extensions.FlavorDimension
import dev.mattramotar.updatingitem.tooling.extensions.Versions
import dev.mattramotar.updatingitem.tooling.extensions.configureAndroid
import dev.mattramotar.updatingitem.tooling.extensions.configureAndroidCompose
import dev.mattramotar.updatingitem.tooling.extensions.configureFlavors

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    targetSdk = Versions.TARGET_SDK
                    missingDimensionStrategy(
                        FlavorDimension.contentType.name,
                        BuildFlavor.demo.name
                    )
                }

                buildFeatures {
                    buildConfig = true
                }

                configureAndroid()
                configureAndroidCompose(this)
                configureFlavors(this)

                buildTypes {
                    getByName(BuildType.DEBUG.applicationIdSuffix) {
                    }

                    getByName(BuildType.RELEASE.applicationIdSuffix) {
                    }
                }
            }
        }
    }
}
