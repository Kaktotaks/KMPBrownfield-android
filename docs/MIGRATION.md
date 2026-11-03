# Brownfield migration log — Android

The Android side of moving the Tasks app onto a Kotlin Multiplatform core one layer at a time, with
the app shippable after every step. The full overview (including the iOS side) is in
[shared's migration log](https://github.com/Kaktotaks/KMPBrownfield-shared/blob/main/docs/MIGRATION.md).

| Step | Android change | Commit |
|---|---|---|
| 0 | Native Jetpack Compose app from the spec | `f8454f7` |
| 1 | `domain/` replaced by shared (composite build) | `0a8d926` |
| 2 | `data/` replaced by shared (Ktor, `TaskRepository`) | `b6402fd` |
| 3 | SKIE — iOS-only, no Android change | — |
| 4 | App ViewModels replaced by shared ViewModels | `b135092` |
| 5 | AGP 9.4.1 / Gradle 9.6.0; stateless UI-only screens | `7c9dea9`, `7048a38` |
| 6 | Versioned dependency from GitHub Packages | `180a4eb` |

## Step 0 — native baseline
Compose + Navigation Compose app with `domain/`, `data/` and per-screen ViewModels, built from the
[spec](https://github.com/Kaktotaks/KMPBrownfield-shared/blob/main/docs/SPEC.md) with the same type
names as the iOS app.

## Step 1 — domain from shared
`domain/` was deleted and imported from `com.example.tasks.shared.domain` via
`includeBuild("../shared")`. `Clock`/`SystemClock` → `DateProvider`/`SystemDateProvider`;
`java.time.LocalDate` → `kotlinx.datetime.LocalDate` (DatePicker UTC millis ↔ `LocalDate` via epoch
days); `TodoDto.toTask` → `TodoMapper.map(id, title, completed, today)`.

## Step 2 — data from shared
`HttpTaskApi` (`HttpURLConnection`), `TodoDto` and `TaskRepository` were replaced by shared's
`KtorTaskApi` and `TaskRepository`/`LoadState` with the same shape, so ViewModels only changed imports.
Ktor and OkHttp come transitively from shared's `androidMain`. On Android this is plain Kotlin:
`collect` the `StateFlow`, call `suspend load()` — no interop layer. kotlinx-serialization stays only
for type-safe navigation routes. Repository tests moved to shared's `commonTest`.

## Step 4 — ViewModels from shared
The four app ViewModels and their UiState types were deleted; the shared ones keep the same public
API. They extend `TasksViewModel : androidx.lifecycle.ViewModel`, so on Android they are regular
ViewModels. The only API difference: `taskId` is a constructor parameter instead of
`SavedStateHandle`; `ViewModelFactories.kt` reads it from the route
(`createSavedStateHandle().toRoute<TaskDetailRoute>().taskId`).

## Step 5 — UI-only screens
AGP 9.4.1 / Gradle 9.6.0. Each screen was split into a stateless `…Screen`, a pure `…UiModel` mapper
(JVM unit-tested) and a stateful `…ScreenRoute` that talks to the shared ViewModel — the same split the
iOS app uses with Swift ViewModels.

## Step 6 — versioned dependency
`includeBuild` → `implementation(libs.shared)` = `com.example.tasks:shared:0.5.0` from GitHub
Packages. The composite build is kept behind `-PuseLocalShared=true` for local development and
token-free builds.

## Possible next steps
Persistence and shared DI (e.g. Koin) to replace the hand-written ViewModel factories.
