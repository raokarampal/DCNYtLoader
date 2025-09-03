rootProject.name = "DCNYtLoader"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val includeComposeApp = providers.systemProperty("includeComposeApp").orElse("true").map { it.toBoolean() }.get()

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

if (includeComposeApp) include(":composeApp")
include(":ytdlbackend")
include(":shared")