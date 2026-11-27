# MaintenanceLog

MaintenanceLog is a small Android application for keeping a personal log of vehicle maintenance. Entries are stored locally with Room and synchronized with a REST API, so the app stays usable without a network connection.

- Package: `com.example.maintenancelog`
- Minimum SDK: 24
- Target / compile SDK: 34
- Language: Kotlin

## Features

- **Full CRUD** - create, view, edit and delete maintenance entries. The delete action is protected by a confirmation dialog.
- **Offline-first synchronization** - Room is the local source of truth; remote REST calls are performed as a best-effort background sync. Pull-to-refresh downloads the remote list into the local cache.
- **Search and filtering** - case-insensitive search over the title and description, combined with category filter chips. The category list is derived from the stored entries.
- **Form validation** - title and category are required, the cost must be a valid non-negative number, and input is trimmed before saving. The date defaults to today when left empty.
- **MaterialDatePicker** - the date field opens a Material 3 date picker; the selected date is stored as local midnight in epoch milliseconds.
- **Cost summary** - the list header shows the number of currently visible entries and their total cost, formatted with the device locale and currency.
- **Light and Dark theme** - Material 3 `DayNight` theme with separate light and dark color palettes. The app follows the system theme automatically.
- **Empty and error states** - dedicated messages for an empty database, an empty filtered result and synchronization failures.

## Screens

1. **Maintenance Log list** (`LogListFragment`)
   - Search field, category filter chips and a summary row.
   - RecyclerView with all entries ordered by date (newest first).
   - Pull-to-refresh for remote synchronization.
   - Floating action button for adding a new entry.
2. **Log detail** (`LogDetailFragment`)
   - Add or edit an entry: title, description, category, date and cost.
   - Category autocomplete with common values (`Engine`, `Oil`, `Tires`, `Brakes`, `Filters`, `Inspection`, `Other`); free text is also accepted.
   - Save validation and a delete action with confirmation for existing entries.

Navigation is handled by the Android Navigation Component (`res/navigation/nav_graph.xml`) with a `logId` argument; `0` means a new entry.

## Technology stack

| Area | Technology |
| --- | --- |
| Language | Kotlin 1.9.24 |
| Architecture | MVVM + Repository, XML layouts with ViewBinding |
| UI | Material Components 1.12.0 (Material 3), ConstraintLayout, RecyclerView, SwipeRefreshLayout |
| Navigation | Navigation Component 2.7.7 |
| Dependency injection | Hilt 2.51.1 with KSP |
| Local storage | Room 2.6.1 (Room KTX) |
| Networking | Retrofit 2.11.0 + Gson converter, OkHttp 4.12.0 |
| Async / state | Kotlin Coroutines 1.8.1, StateFlow |
| Build | Android Gradle Plugin 8.5.2, Gradle 8.7, JDK 17 |
| Testing | JUnit 4.13.2, kotlinx-coroutines-test, AndroidX Test, Room testing |

Versions are managed centrally in `gradle/libs.versions.toml`.

## Architecture and offline-first flow

```text
Fragment (ViewBinding)
        |
        v
ViewModel (StateFlow)
        |
        v
MaintenanceRepository
   |                 |
   v                 v
 Room database     Retrofit API
 (source of truth) (background sync)
```

- Hilt provides the database, DAO, API service and repository through `DatabaseModule`, `NetworkModule` and `RepositoryModule`.
- The UI observes `StateFlow` from `LogListViewModel` and `LogDetailViewModel`; fragments render state and forward user actions to the ViewModels.
- `MaintenanceRepositoryImpl` exposes the Room `Flow` of entries. The list ViewModel applies text/category filters and recalculates the total cost for the visible entries.
- Writes go to Room first. The repository then calls the REST API from a background `CoroutineScope`; create/update/delete synchronization is best-effort and does not block the UI.
- Pull-to-refresh calls `GET /maintenance-logs`, maps DTOs with a non-null id to Room entities and upserts them with `insertAll` (`REPLACE`). The Room `Flow` then updates the UI automatically.
- If refresh fails, the cached entries remain visible and the error is shown in a Snackbar. The rest of the app continues to work offline.

### Local database

- Entity: `MaintenanceLogEntity` (`id`, `title`, `description`, `category`, `date`, `cost`).
- DAO: `MaintenanceLogDao` - ordered `getAll()` Flow, `getById`, `insert`, `insertAll` (upsert), `update`, `delete`, `deleteById`.
- Database: `AppDatabase`, database name `maintenance_log.db`, schema version 1.

### REST API

The Retrofit service uses the base URL defined in `NetworkModule` (currently the placeholder `https://example.com/api/`). Replace it with the real backend URL before testing synchronization.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `maintenance-logs` | Fetch all entries |
| `GET` | `maintenance-logs/{id}` | Fetch one entry |
| `POST` | `maintenance-logs` | Create an entry |
| `PUT` | `maintenance-logs/{id}` | Update an entry |
| `DELETE` | `maintenance-logs/{id}` | Delete an entry |

HTTP body logging is enabled in debug builds and disabled in release builds.

## Project structure

```text
app/src/main/java/com/example/maintenancelog/
├── MainActivity.kt
├── MaintenanceLogApplication.kt
├── LogListFragment.kt
├── LogDetailFragment.kt
├── data/
│   ├── local/        # Room entity, DAO and AppDatabase
│   ├── remote/       # Retrofit service, DTO and mappers
│   └── repository/   # Repository contract and implementation
├── di/               # Hilt modules
└── ui/               # ViewModels, UI states and RecyclerView adapter

app/src/test/java/com/example/maintenancelog/       # JVM unit tests and fakes
app/src/androidTest/java/com/example/maintenancelog/ # Room DAO instrumented tests
```

## Build and run

### Requirements

- Android Studio with Android SDK 34 installed
- JDK 17 (bundled with recent Android Studio versions)
- An emulator or physical device running Android 7.0 (API 24) or newer

### Build the debug APK

```powershell
.\gradlew.bat :app:assembleDebug
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

On macOS/Linux use `./gradlew` instead of `.\gradlew.bat`.

### Run the app

Open the project in Android Studio, select the `app` run configuration, choose an emulator or connected device and press **Run**. Alternatively install the debug build from the command line:

```powershell
.\gradlew.bat :app:installDebug
```

## Tests

### JVM unit tests

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

These tests use `kotlinx-coroutines-test`, `MainDispatcherRule` and in-memory fakes. They cover:

- list loading, summary calculation, text search, category filtering and sync failures (`LogListViewModelTest`);
- validation, create/update/load/delete flows (`LogDetailViewModelTest`);
- Room/remote repository behavior and refresh error handling (`MaintenanceLogRepositoryTest`).

An HTML report is available at `app/build/reports/tests/testDebugUnitTest/index.html`.

### Room DAO instrumented tests

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

The DAO tests use `Room.inMemoryDatabaseBuilder(...)` and close the database after each test. They cover insert and Flow reads, upsert via `insertAll`, updates, deletion by entity and id, date ordering and empty-database edge cases. A running emulator or connected device is required.

To only compile the instrumented test APK without a device:

```powershell
.\gradlew.bat :app:assembleDebugAndroidTest
```

## Notes

- The REST base URL is a placeholder; synchronization requires a compatible backend.
- Local Room data is the primary source of truth. Remote create/update/delete calls are best-effort, so local changes remain available even when the API is unreachable.
- The database currently has schema version 1 and no migrations; destructive schema changes require a migration or a version bump.
