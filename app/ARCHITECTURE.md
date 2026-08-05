# SmartBiz — Clean Architecture & Engineering Documentation

SmartBiz is engineered with a modular **Clean Architecture** to ensure high maintainability, testability, offline reliability, and clean separation of concerns.

---

## 🏛️ Architecture Layers

```
+-------------------------------------------------------------------+
|                        PRESENTATION LAYER                         |
|  - Jetpack Compose Screens & Reusable UI Components               |
|  - ViewModels (Exposing Immutable StateFlow UI States)            |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                           DOMAIN LAYER                            |
|  - Repository Interfaces & Contracts                              |
|  - Business Validation Rules (Validators, Formatters)            |
|  - State Data Models & Search Data Contracts                      |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                            DATA LAYER                             |
|  - Repository Concrete Implementations                            |
|  - Room Local Database (AppDatabase, DAOs, Entities)             |
|  - Local Storage (SharedPreferencesService, File Export Service)  |
+-------------------------------------------------------------------+
```

---

## 🔑 Architectural Principles

1. **Unidirectional Data Flow (UDF)**:
   - UI Composable triggers User Actions on ViewModel.
   - ViewModel updates private `MutableStateFlow` and exposes public immutable `StateFlow`.
   - UI observes state changes via `collectAsStateWithLifecycle()` and recomposes accordingly.

2. **Repository Pattern**:
   - All data operations pass through explicit Repository classes (`InvoiceRepository`, `InventoryRepository`, `CustomerRepository`, etc.).
   - Repositories encapsulate Room DAO queries and SharedPreferences lookups.

3. **Offline First**:
   - All business operations execute locally against SQLite database transactions.
   - No internet connection required.

4. **Single Source of Truth**:
   - Room Database serves as the central data store.
   - Reactive Kotlin `Flow` streams automatically update the UI when database records change.

---

## 📂 Complete Module Breakdown

- `core/database`: Room ORM setup, entity definitions, migration strategies, database helper, and optimization routines (`PRAGMA WAL`, `VACUUM`, `ANALYZE`).
- `core/services`: Persistence helpers for application settings, invoice preferences, backup creation/restoration, and CSV file formatting.
- `core/utils`: Shared utilities including string formatters, currency formatting, input regex validators, and exception handlers.
- `features/*`: Feature-packaged UI modules containing Composable screens, ViewModels, and state data classes.
- `shared/components`: High-level layout components like `AppShell`, top header bars, and navigation drawers.
- `shared/widgets`: Standardized Material 3 UI widgets (skeletons, empty states, error cards, filter chips).
