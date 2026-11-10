import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // AGP 9 has built-in Kotlin support: no separate org.jetbrains.kotlin.android plugin.
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    // Serialization stays for type-safe Navigation routes (@Serializable in ui/navigation/Routes.kt).
    // JSON/networking (TodoDto, Ktor) now live in the shared module.
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    namespace = "com.example.tasks.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.tasks.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // KMP shared module: domain (Task, TaskValidator, TaskFilter, StatsCalculator, ...),
    // data (TaskApi/KtorTaskApi, TaskRepository, LoadState) and presentation (the four ViewModels +
    // UiState types; shared exposes androidx.lifecycle:lifecycle-viewmodel as `api`). Ktor + the OkHttp engine come
    // transitively from shared's androidMain, so they are not declared here.
    // Versioned artifact from GitHub Packages (libs.versions.toml → shared). With
    // -PuseLocalShared=true the composite build substitutes it with ../shared sources.
    // kotlinx-datetime comes transitively (shared exposes it as `api`).
    implementation(libs.shared)

    implementation(libs.androidx.core.ktx)
    // Launch frame: plain #0A0A0A (Theme.App.Starting), hands over to the in-app SplashOverlay at once.
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Map tab: MapLibre Native (vector tiles + GeoJSON route layers) in an AndroidView; pins are a Compose overlay.
    implementation(libs.maplibre.android)

    implementation(libs.kotlinx.coroutines.android)
    // Runtime for the @Serializable navigation routes (not for networking anymore).
    implementation(libs.kotlinx.serialization.json)

    // JVM unit tests for the pure UI-model mappers (ui/*/…UiModel.kt): no Android/Compose runtime needed.
    testImplementation(libs.junit)
}
