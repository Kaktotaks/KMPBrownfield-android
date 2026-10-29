# TasksAndroid — point 0 (native Jetpack Compose)

Native Android twin of the iOS Tasks app, built from `../SPEC.md`. **No KMP yet**: this is the
"brownfield" starting point. Domain + Data are written in plain Kotlin (no `android.*` imports)
so they can later move to a KMP `shared` module (`commonMain`).

## Stack
- AGP 9.1.1 (built-in Kotlin), Kotlin 2.4.21, Gradle 9.5.1, compileSdk/targetSdk 37, minSdk 26
- Jetpack Compose (BOM 2026.09.00, Material 3), Navigation Compose 2.10.2 (type-safe `@Serializable` routes)
- androidx Lifecycle 2.11.0 (`ViewModel` + `StateFlow`, `viewModelFactory { initializer { } }`)
- `HttpURLConnection` + kotlinx.serialization 1.12.0, kotlinx.coroutines 1.11.0
- Manual DI: `AppContainer` in `TasksApplication` (one `TaskApi`, one `TaskRepository`, one `Clock`)

## Structure (`app/src/main/kotlin/com/example/tasks/android/`)
```
domain/      Task, Priority, TaskValidator, ValidationError, TaskFilter (+ StatusFilter, SortOrder),
             StatsCalculator, TaskStats, Clock, DateFormatting        <- future KMP shared
data/        TaskApi (+ HttpTaskApi), TodoDto (+ mapping), TaskRepository (+ LoadState) <- future KMP shared
ui/tasklist      TaskListScreen + TaskListViewModel
ui/taskdetail    TaskDetailScreen + TaskDetailViewModel
ui/taskeditor    TaskEditorScreen + TaskEditorViewModel (full-screen dialog destination ~ iOS sheet)
ui/stats         StatsScreen + StatsViewModel
ui/components    PriorityBadge, load/error/empty states, ViewModel factory helper
ui/navigation    Routes + TasksRoot (Scaffold + NavigationBar + NavHost)
ui/theme         Material 3 theme + semantic colors
MainActivity.kt, TasksApplication.kt
```
Unit tests: `app/src/test/kotlin/...` — `TaskValidatorTest`, `TaskFilterTest`, `StatsCalculatorTest`
(fixed `Clock`, today = 29 Oct 2026) and `TodoDtoMappingTest`.

## Open & run
1. Android Studio → **Open** → select the `android-app` folder. Let Gradle sync.
2. Pick an emulator/device (API 26+) and run the `app` configuration.

The app loads 20 todos from `https://jsonplaceholder.typicode.com/todos?_limit=20` on start; afterwards
all changes (add / edit / toggle / delete) live in memory only.

## Tests
```
./gradlew :app:testDebugUnitTest
```

## KMP notes
- `domain/` and `data/` are the candidates for the future `shared` module.
- `java.time.LocalDate` is JVM-only → becomes `kotlinx-datetime` in `commonMain`.
- `HttpTaskApi` (`HttpURLConnection`) → Ktor client; `TaskApi` interface stays.
