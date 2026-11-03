# TasksAndroid — KMP step 4 (domain + data + ViewModels from `../shared`)

Native Android twin of the iOS Tasks app, built from `../SPEC.md`. Brownfield KMP migration,
step 4: the **domain**, **data** and **presentation** (ViewModels + UiState) layers now come from the
Kotlin Multiplatform library in `../shared` (see "Shared (KMP) integration" below). The app itself is
only UI (stateless Compose screens + their UI models), navigation and DI (`AppContainer` + ViewModel factories).

## Stack
- AGP 9.4.1 (built-in Kotlin), Kotlin 2.4.21, Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26
- Jetpack Compose (BOM 2026.09.00, Material 3), Navigation Compose 2.10.2 (type-safe `@Serializable` routes)
- androidx Lifecycle 2.11.0 (`ViewModel` + `StateFlow`, `viewModelFactory { initializer { } }`)
- kotlinx.coroutines 1.11.0; kotlinx.serialization 1.12.0 (only for navigation routes now)
- Manual DI: `AppContainer` in `TasksApplication` (one `TaskApi`, one `TaskRepository`, one `DateProvider`)
- `shared` KMP library (GitHub Packages, `0.5.0`; composite build via `-PuseLocalShared=true`): domain + data (Ktor client, OkHttp engine on Android),
  kotlinx-datetime 0.8.0 (transitive, `LocalDate`)

## Structure (`app/src/main/kotlin/com/example/tasks/android/`)
```
ui/tasklist      TaskListScreenRoute, TaskListScreen, TaskListUiModel (+ TaskRowUi)
ui/taskdetail    TaskDetailScreenRoute, TaskDetailScreen, TaskDetailUiModel
ui/taskeditor    TaskEditorScreenRoute, TaskEditorScreen, TaskEditorUiModel (+ DatePicker millis <-> LocalDate)
                 (full-screen dialog destination ~ iOS sheet)
ui/stats         StatsScreenRoute, StatsScreen, StatsUiModel (+ StatRowUi)
ui/components    PriorityBadge + PriorityUi (Priority.badgeColor), SelectableOption, load/error/empty states,
                 ViewModelFactories (Android-only DI for shared VMs)
ui/navigation    Routes + TasksRoot (Scaffold + NavigationBar + NavHost)
ui/theme         Material 3 theme + semantic colors
MainActivity.kt, TasksApplication.kt
```
Domain classes (`Task`, `Priority`, `TaskValidator`, `ValidationError`, `TaskFilter` + `StatusFilter` /
`SortOrder`, `StatsCalculator` + `TaskStats`, `DateProvider` / `SystemDateProvider`, `DateFormatting`,
`TodoMapper`) are imported from `com.example.tasks.shared.domain`; the data layer (`TaskApi`,
`KtorTaskApi`, `TaskRepository`, `LoadState`) from `com.example.tasks.shared.data`; the ViewModels
(`TaskListViewModel`, `TaskDetailViewModel`, `TaskEditorViewModel`, `StatsViewModel`) and their UiState
types from `com.example.tasks.shared.presentation`.

## UI layering: screens are UI-only
Each screen is split into three app-side pieces on top of the shared ViewModel (the Compose
"stateful route + stateless screen" idiom; the iOS app uses the same split with a Swift screen model):

```
TaskListScreen        stateless Compose UI: layout only                       (ui/tasklist/TaskListScreen.kt)
      ^  TaskListUiModel + event lambdas
TaskListUiModel       pure Kotlin mapping, no Compose: formatting, labels,    (ui/tasklist/TaskListUiModel.kt)
      ^  TaskListUiState.toUiModel()   selected options, Loading/Error/Empty/Loaded
TaskListScreenRoute   stateful: viewModel(factory = …), collectAsStateWithLifecycle(),
      ^               remember(state) { state.toUiModel() }, wires viewModel::onX into the lambdas
TaskListViewModel     shared (../shared, commonMain): business/presentation logic + UiState
```

- **Screen** (`…Screen.kt`): receives only the UI model and lambdas. No ViewModel, no repository, no
  `DateFormatting`/`LocalDate`, no `when` over domain/UiState types, no string building. Sub-areas are small
  private composables taking UI data (`TaskRow(row: TaskRowUi)`, `SortMenu`, `StatusFilterRow`,
  `DeleteConfirmationDialog`, `DueDatePickerDialog`, `StatRow`). Purely visual local state stays here
  (sort menu open, delete confirmation, date picker visible). Every screen has `@Preview`s built from sample
  UI models — possible precisely because the screen does not need a ViewModel.
- **UI model** (`…UiModel.kt`): `data class`/`sealed interface` with everything already formatted
  (`"Due 29 Oct 2026"`, `isOverdue`, `"Done"`/`"Active"`, `"Mark as done"`, `"No notes"`, `"75%"`,
  error messages, `SelectableOption(value, label, isSelected)` for segmented buttons / sort menu) and a pure
  `UiState.toUiModel()` mapper. The editor file also holds the DatePicker conversion
  (`LocalDate.toUtcMillis()` / `utcMillisToLocalDate()`). Unit-tested on the JVM (`app/src/test`).
- **Route** (`…ScreenRoute.kt`): the only composable that touches the shared ViewModel. Also translates UI
  events back into domain calls: DatePicker millis -> `LocalDate`, "Save" -> `if (viewModel.save()) onDismiss()`,
  "Delete" -> `viewModel.onDelete()` + pop back, "Edit" -> `onEdit(viewModel.taskId)`. `TasksRoot` hosts the
  routes. (Named `…ScreenRoute` because `TaskListRoute`, `TaskDetailRoute`, … are the navigation destinations.)
- **Shared ViewModel**: unchanged; filtering, sorting, validation, stats and repository access stay in `shared`.
- `Priority.badgeColor()` (`ui/components/PriorityUi.kt`) is the one tiny presentation helper that needs
  Compose's `Color`; `PriorityBadge` only lays it out.

Unit tests: `app/src/test` has JVM tests for the UI-model mappers (`TaskListUiModelTest`,
`TaskDetailUiModelTest`, `TaskEditorUiModelTest`, `StatsUiModelTest`); domain, repository and ViewModel tests
live in `../shared/src/commonTest`.

## Open & run
1. Make sure `shared/` sits next to `android-app/` (`../shared`), then Android Studio → **Open** →
   select the `android-app` folder. Let Gradle sync (the `shared` build is included automatically).
2. Pick an emulator/device (API 26+) and run the `app` configuration.

The app loads 20 todos from `https://jsonplaceholder.typicode.com/todos?_limit=20` on start; afterwards
all changes (add / edit / toggle / delete) live in memory only.

## Tests
- App: UI-model mapper tests — `./gradlew :app:testDebugUnitTest` (JUnit 4, plain JVM).
- Shared: domain / data / ViewModel tests — from `../shared`, e.g. `./gradlew allTests`.

## Shared (KMP) integration
The domain and data layers live in the standalone Gradle build `../shared` (Kotlin Multiplatform: Android +
iOS). The app consumes it as a **versioned Maven artifact** (step 6), with a composite build as a local-dev switch:

- Default: `implementation(libs.shared)` → `com.example.tasks:shared:0.5.0` (`gradle/libs.versions.toml`)
  from **GitHub Packages**. Gradle reads the Gradle module metadata of the KMP publication and picks the
  Android variant (`shared-android`). Kotlin/Native and the iOS part are not built at all.
- GitHub Packages needs a token even for public packages — `~/.gradle/gradle.properties`:
  ```properties
  GitHubPackagesUsername=<github-login>
  GitHubPackagesPassword=<classic PAT with read:packages>
  ```
- Updating shared = bump `shared` in `libs.versions.toml` in its own PR.
- Local development: `./gradlew … -PuseLocalShared=true` (or `useLocalShared=true` in
  `~/.gradle/gradle.properties`). `settings.gradle.kts` then does `includeBuild("../shared")`, which
  substitutes `com.example.tasks:shared` (any version) with the `../shared` sources:
  ```
  KMPBrownfield/
    android-app/   <- this project
    shared/        <- KMP library (group "com.example.tasks", rootProject.name "shared")
    ios-app/
  ```
- `kotlinx-datetime` is an `api` dependency of `shared` (`Task.dueDate` is a `LocalDate`), so the
  app gets it transitively and does not declare it.
- Step 4 — ViewModels from `shared` (`com.example.tasks.shared.presentation`):
  - `TaskListViewModel(repository, dateProvider)`, `TaskDetailViewModel(taskId, repository, dateProvider)`,
    `TaskEditorViewModel(taskId, repository, validator, dateProvider)`, `StatsViewModel(repository, dateProvider)`
    + `TaskListUiState`/`TaskListContent`, `TaskDetailUiState`, `TaskEditorUiState`, `StatsUiState`. Same
    public API as the old app VMs, so screens only changed imports and the `factory =` argument.
  - All extend `TasksViewModel : androidx.lifecycle.ViewModel` (shared declares `lifecycle-viewmodel`
    as `api`). On Android they are **normal androidx ViewModels**: created by `ViewModelProvider`, scoped
    to the `NavBackStackEntry`, survive configuration changes, `viewModelScope` cancelled in `onCleared()`.
  - The only change in the VMs: no `SavedStateHandle` (it is Android-only) — `taskId` is a constructor
    parameter. `TaskEditorRoute.NEW_TASK_ID` forwards `TaskEditorViewModel.NEW_TASK_ID` (`-1`).
  - What stays in the app (Android-specific DI): `ui/components/ViewModelFactories.kt` —
    `TaskListViewModelFactory`, `TaskDetailViewModelFactory`, `TaskEditorViewModelFactory`,
    `StatsViewModelFactory` (`viewModelFactory { initializer { ... } }`). They take dependencies from
    `AppContainer` and decode the route with `createSavedStateHandle().toRoute<TaskDetailRoute>().taskId`.
    The `…ScreenRoute` composables call `viewModel(factory = TaskListViewModelFactory)` +
    `collectAsStateWithLifecycle()` (see "UI layering" above).
  - Deleted: `ui/*/…ViewModel.kt` (4 files, incl. their UiState types) and `ui/components/AppViewModelFactory.kt`
    (its `CreationExtras.appContainer()` helper moved into `ViewModelFactories.kt`).
  - On iOS the same classes are used without a `ViewModelStoreOwner`, so the Swift side must call
    `clear()` itself — on Android nothing like that is needed.
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
- Step 6: `includeBuild` → versioned dependency from GitHub Packages (composite build kept behind `useLocalShared`).

## KMP notes
- Done (step 1): `domain/` → `../shared` (`commonMain`), `java.time` → `kotlinx-datetime`.
- Done (step 2): `data/` → `../shared` — `HttpTaskApi` (`HttpURLConnection`) → `KtorTaskApi`;
  `TaskApi` interface, `TaskRepository` and `LoadState` are shared.
- Done (step 4): ViewModels + UiState → `../shared` (`presentation`); the app keeps UI, navigation and
  the ViewModel factories.
- Next candidates: persistence, shared DI (e.g. Koin) to replace the hand-written factories.
