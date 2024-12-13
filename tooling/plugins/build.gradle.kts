plugins {
    `kotlin-dsl`
}

group = "dev.mattramotar.updatingitem.tooling"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.dokka.gradle.plugin)
    compileOnly(libs.maven.publish.plugin)
    implementation(libs.mokkery.gradle)
    implementation(libs.kover.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationPlugin") {
            id = "plugin.updatingitem.android.application"
            implementationClass = "dev.mattramotar.updatingitem.tooling.plugins.AndroidApplicationConventionPlugin"
        }

        register("androidComposePlugin") {
            id = "plugin.updatingitem.android.compose"
            implementationClass = "dev.mattramotar.updatingitem.tooling.plugins.AndroidComposeConventionPlugin"
        }

        register("androidLibraryPlugin") {
            id = "plugin.updatingitem.android.library"
            implementationClass = "dev.mattramotar.updatingitem.tooling.plugins.AndroidLibraryConventionPlugin"
        }

        register("kotlinAndroidLibraryPlugin") {
            id = "plugin.updatingitem.kotlin.android.library"
            implementationClass = "dev.mattramotar.updatingitem.tooling.plugins.KotlinAndroidLibraryConventionPlugin"
        }

        register("kotlinMultiplatformPlugin") {
            id = "plugin.updatingitem.kotlin.multiplatform"
            implementationClass = "dev.mattramotar.updatingitem.tooling.plugins.KotlinMultiplatformConventionPlugin"
        }
    }
}