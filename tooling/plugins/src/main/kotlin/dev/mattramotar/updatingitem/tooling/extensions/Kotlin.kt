package dev.mattramotar.updatingitem.tooling.extensions
import dev.mattramotar.updatingitem.tooling.extensions.configureJava
import org.gradle.api.Project

fun Project.configureKotlin() {
  configureJava()
}