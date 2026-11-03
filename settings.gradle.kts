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

// Where `shared` comes from — two modes:
//
// 1) Default: the published artifact com.example.tasks:shared:<version> from GitHub Packages
//    (version: libs.versions.toml → shared). Kotlin/Native and the iOS part are not built at all.
//    GitHub Packages requires a token even for public packages, in ~/.gradle/gradle.properties:
//      GitHubPackagesUsername=<login>
//      GitHubPackagesPassword=<classic PAT with read:packages>
//
// 2) Sources from ../shared: -PuseLocalShared=true (or useLocalShared=true in
//    ~/.gradle/gradle.properties). The composite build substitutes com.example.tasks:shared
//    with the ../shared sources (the version is ignored). No token needed.
val useLocalShared = providers.gradleProperty("useLocalShared").orNull == "true"

if (useLocalShared) {
    includeBuild("../shared")
} else {
    dependencyResolutionManagement {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/Kaktotaks/KMPBrownfield-shared")
                credentials(PasswordCredentials::class)
                // Only our artifacts: other dependencies never hit GitHub Packages.
                content { includeGroup("com.example.tasks") }
            }
        }
    }
}

include(":app")
