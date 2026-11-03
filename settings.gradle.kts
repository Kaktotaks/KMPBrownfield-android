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

// ⚡️ Звідки береться shared — два режими:
//
// 1) За замовчуванням: опублікований артефакт com.example.tasks:shared:<version> з GitHub Packages
//    (версія — libs.versions.toml → shared). Kotlin/Native та iOS-частина не збираються взагалі.
//    GitHub Packages вимагає токен навіть для публічних пакетів: у ~/.gradle/gradle.properties
//      GitHubPackagesUsername=<login>
//      GitHubPackagesPassword=<classic PAT з read:packages>
//
// 2) Локальна розробка shared разом із застосунком: -PuseLocalShared=true
//    (або useLocalShared=true у ~/.gradle/gradle.properties). Composite build підміняє
//    координати com.example.tasks:shared вихідним кодом з ../shared — версія ігнорується.
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
                // Лише наші артефакти: решта залежностей не ходить у GitHub Packages.
                content { includeGroup("com.example.tasks") }
            }
        }
    }
}

include(":app")
