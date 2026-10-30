# TasksAndroid — KMP step 2 (domain + data from `../shared`)

Native Android twin of the iOS Tasks app, built from `../SPEC.md`. Brownfield KMP migration,
step 2: the **domain** and **data** layers now come from the Kotlin Multiplatform library in
`../shared` (see "Shared (KMP) integration" below). Only UI/ViewModels and DI stay in the app.

## Stack
- AGP 9.1.1 (built-in Kotlin), Kotlin 2.4.21, Gradle 9.5.1, compileSdk/targetSdk 37, minSdk 26
- Jetpack Compose (BOM 2026.09.00, Material 3), Navigation Compose 2.10.2 (type-safe `@Serializable` routes)
- androidx Lifecycle 2.11.0 (`ViewModel` + `StateFlow`, `viewModelFactory { initializer { } }`)
- kotlinx.coroutines 1.11.0; kotlinx.serialization 1.12.0 (only for navigation routes now)
- Manual DI: `AppContainer` in `TasksApplication` (one `TaskApi`, one `TaskRepository`, one `DateProvider`)
- `shared` KMP library (composite build): domain + data (Ktor client, OkHttp engine on Android),
  kotlinx-datetime 0.8.0 (transitive, `LocalDate`)

## Structure (`app/src/main/kotlin/com/example/tasks/android/`)
```
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
`TodoMapper`) are imported from `com.example.tasks.shared.domain`; the data layer (`TaskApi`,
`KtorTaskApi`, `TaskRepository`, `LoadState`) from `com.example.tasks.shared.data`.

Unit tests: the app has none left — domain and repository tests live in `../shared/src/commonTest`.

## Open & run
1. Make sure `shared/` sits next to `android-app/` (`../shared`), then Android Studio → **Open** →
   select the `android-app` folder. Let Gradle sync (the `shared` build is included automatically).
2. Pick an emulator/device (API 26+) and run the `app` configuration.

The app loads 20 todos from `https://jsonplaceholder.typicode.com/todos?_limit=20` on start; afterwards
all changes (add / edit / toggle / delete) live in memory only.

## Tests
All unit tests are in the shared module (run them from `../shared`, e.g. `./gradlew allTests`).

## Shared (KMP) integration
The domain and data layers live in the standalone Gradle build `../shared` (Kotlin Multiplatform: Android +
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
- Step 2 — data layer from `shared` (`com.example.tasks.shared.data`):
  - `TaskApi` + `KtorTaskApi()` — one Ktor client (ContentNegotiation + kotlinx-json, timeouts,
    `expectSuccess`) replaces `HttpTaskApi` (`HttpURLConnection`). The HTTP code is common; only the
    engine is platform-specific: OkHttp comes transitively from shared's `androidMain`, so the app
    declares neither Ktor nor OkHttp. `TodoDto` is `internal` to shared.
  - `TaskRepository(api, dateProvider)` with `state: StateFlow<LoadState>`, `load()`, `task(id)`,
    `add`/`update`/`toggle`/`delete`; `sealed interface LoadState { Loading; Error; Loaded(tasks) }` —
    same shape as the old app types, so ViewModels only changed imports.
  - `AppContainer`: `taskApi = KtorTaskApi()`, `TaskRepository(taskApi, dateProvider)`.
  - Deleted: `data/` (`TaskApi.kt`, `TodoDto.kt`, `TaskRepository.kt`) and `TaskRepositoryTest`
    (repository tests are in `../shared/src/commonTest`).
  - On Android this is plain Kotlin: ViewModels `collect` the `StateFlow` and call `suspend load()`
    directly — **no interop layer needed**. Contrast with iOS, where Swift sees `StateFlow` without its
    generic (hence `currentState`), the sealed `LoadState` as separate classes rather than an enum,
    and `suspend` as completion-handler/async bridges.
  - The kotlinx-serialization plugin + runtime stay in the app only for type-safe navigation routes.
- Step 1 — what changed in the app: `domain/` was deleted; `Clock`/`SystemClock` → `DateProvider` /
  `SystemDateProvider`; `java.time.LocalDate` → `kotlinx.datetime.LocalDate` (DatePicker UTC millis
  ↔ `LocalDate` via epoch days); `TodoDto.toTask` → `TodoMapper.map(id, title, completed, today)`.
- Later: when `shared` is published to a Maven repository, remove the `includeBuild` line and add a
  version: `implementation("com.example.tasks:shared:0.1.0")`.

## KMP notes
- Done (step 1): `domain/` → `../shared` (`commonMain`), `java.time` → `kotlinx-datetime`.
- Done (step 2): `data/` → `../shared` — `HttpTaskApi` (`HttpURLConnection`) → `KtorTaskApi`;
  `TaskApi` interface, `TaskRepository` and `LoadState` are shared.
- Next candidates: shared presentation logic (ViewModel-like state holders), persistence.
