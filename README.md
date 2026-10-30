# TasksAndroid — KMP step 1 (domain from `../shared`)

Native Android twin of the iOS Tasks app, built from `../SPEC.md`. Brownfield KMP migration,
step 1: the **domain** layer now comes from the Kotlin Multiplatform library in `../shared`
(see "Shared (KMP) integration" below). Data (network/repository) and UI/ViewModels stay in the app.

## Stack
- AGP 9.1.1 (built-in Kotlin), Kotlin 2.4.21, Gradle 9.5.1, compileSdk/targetSdk 37, minSdk 26
- Jetpack Compose (BOM 2026.09.00, Material 3), Navigation Compose 2.10.2 (type-safe `@Serializable` routes)
- androidx Lifecycle 2.11.0 (`ViewModel` + `StateFlow`, `viewModelFactory { initializer { } }`)
- `HttpURLConnection` + kotlinx.serialization 1.12.0, kotlinx.coroutines 1.11.0
- Manual DI: `AppContainer` in `TasksApplication` (one `TaskApi`, one `TaskRepository`, one `DateProvider`)
- `shared` KMP library (composite build) + kotlinx-datetime 0.8.0 (transitive, `LocalDate`)

## Structure (`app/src/main/kotlin/com/example/tasks/android/`)
```
data/        TaskApi (+ HttpTaskApi), TodoDto, TaskRepository (+ LoadState) <- future KMP shared (step 2)
ui/tasklist      TaskListScreen + TaskListViewModel
ui/taskdetail    TaskDetailScreen + TaskDetailViewModel
ui/taskeditor    TaskEditorScreen + TaskEditorViewModel (full-screen dialog destination ~ iOS sheet)
ui/stats         StatsScreen + StatsViewModel
ui/components    PriorityBadge, load/error/empty states, ViewModel factory helper
ui/navigation    Routes + TasksRoot (Scaffold + NavigationBar + NavHost)
ui/theme         Material 3 theme + semantic colors
MainActivity.kt, TasksApplication.kt
```
Domain classes (`Task`, `Priority`, `TaskValidator`, `ValidationError`, `TaskFilter` + `StatusFilter` /
`SortOrder`, `StatsCalculator` + `TaskStats`, `DateProvider` / `SystemDateProvider`, `DateFormatting`,
`TodoMapper`) are imported from `com.example.tasks.shared.domain`.

Unit tests: `app/src/test/kotlin/...` — `TaskRepositoryTest` (fake `TaskApi`, fixed `DateProvider`,
today = 29 Oct 2026). The domain tests moved to `../shared/src/commonTest`.

## Open & run
1. Make sure `shared/` sits next to `android-app/` (`../shared`), then Android Studio → **Open** →
   select the `android-app` folder. Let Gradle sync (the `shared` build is included automatically).
2. Pick an emulator/device (API 26+) and run the `app` configuration.

The app loads 20 todos from `https://jsonplaceholder.typicode.com/todos?_limit=20` on start; afterwards
all changes (add / edit / toggle / delete) live in memory only.

## Tests
```
./gradlew :app:testDebugUnitTest
```

## Shared (KMP) integration
The domain layer lives in the standalone Gradle build `../shared` (Kotlin Multiplatform: Android +
iOS). The app consumes it through a **Gradle composite build**:

- `settings.gradle.kts`: `includeBuild("../shared")`. The folder layout is therefore fixed:
  ```
  KMPBrownfield/
    android-app/   <- this project
    shared/        <- KMP library (group "com.example.tasks", rootProject.name "shared")
    ios-app/
  ```
- `app/build.gradle.kts`: `implementation("com.example.tasks:shared")` — no version.
- Substitution: the included build publishes the coordinates `group:rootProject.name`
  = `com.example.tasks:shared`. Gradle sees that the included build produces exactly this module and
  replaces the external dependency with the local project, so `:app` compiles against the current
  `shared` sources (Android variant of the `com.android.kotlin.multiplatform.library` target). Changes in
  `../shared` are picked up on the next build — no publishing, no `mavenLocal()`.
- `kotlinx-datetime` is an `api` dependency of `shared` (`Task.dueDate` is a `LocalDate`), so the
  app gets it transitively and does not declare it.
- What changed in the app: `domain/` was deleted; `Clock`/`SystemClock` → `DateProvider` /
  `SystemDateProvider`; `java.time.LocalDate` → `kotlinx.datetime.LocalDate` (DatePicker UTC millis
  ↔ `LocalDate` via epoch days); `TodoDto.toTask` → `TodoMapper.map(id, title, completed, today)`.
- Later: when `shared` is published to a Maven repository, remove the `includeBuild` line and add a
  version: `implementation("com.example.tasks:shared:0.1.0")`.

## KMP notes
- Done (step 1): `domain/` → `../shared` (`commonMain`), `java.time` → `kotlinx-datetime`.
- Next: `data/` is the candidate for the next step.
- `HttpTaskApi` (`HttpURLConnection`) → Ktor client; `TaskApi` interface stays.
