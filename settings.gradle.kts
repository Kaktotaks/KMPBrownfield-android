rootProject.name = "TasksAndroid"

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

// Composite build: the KMP library in ../shared is a separate, standalone Gradle build.
// includeBuild makes Gradle substitute the external coordinates "com.example.tasks:shared"
// (group from shared/build.gradle.kts + rootProject.name "shared") with the local source build,
// so the app always compiles against the current shared sources - no publishing step.
// Later, when shared is published to a Maven repository, this line is removed and the
// dependency in app/build.gradle.kts gets a version.
includeBuild("../shared")

include(":app")
