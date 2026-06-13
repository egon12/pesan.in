# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pesan.in is an Android ordering/inventory app (package `org.egon12.pesanin`) built with Kotlin, Jetpack Compose, Room, and Hilt.

## Build & Quality Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew testDebugUnitTest      # Run all unit tests
./gradlew testDebugUnitTest --tests "org.egon12.pesanin.FooTest"  # Run one test class
./gradlew connectedDebugAndroidTest  # Run instrumented tests
./gradlew ktlintCheck            # Check code style
./gradlew ktlintFormat           # Auto-fix code style
```

## Architecture

The app follows a single-module MVVM architecture:

```
app/src/main/java/org/egon12/pesanin/
├── core/          # Room AppDatabase, Hilt AppModule, TypeConverters
├── dao/           # Room DAOs (OrderDao, OrderItemDao, ProductDao)
├── model/         # Room entities (Order, OrderItem, Product, Invoice)
├── repository/    # Data layer — Room repos + SettingsRepository (SharedPreferences)
├── viewmodels/    # HiltViewModel classes; emit UiEvent via MutableSharedFlow
├── screen/        # Compose screens, Navigation.kt (Screen sealed class + NavHost)
└── ui/theme/      # Material 3 theme
```

**Navigation:** `Screen` is a sealed class in `Navigation.kt` defining all routes. `MainScreen` hosts a `Scaffold` with `PesaninNavHost`, `PesaninTopBar`, `PesaninNavBar`, and `PesaninFAB`. Navigation events flow through `MainViewModel.events: MutableSharedFlow<UiEvent>`.

**ViewModel → UI event bus:** `MainViewModel` emits `UiEvent` (Navigate, NavigateBack, Snackbar) collected in `MainScreen` via `LaunchedEffect`. Screens call `viewModel.navigate(Screen.X)` or `viewModel.alert(msg)` rather than controlling the nav controller directly. Screen-specific side effects use their own sealed classes (e.g., `CreateOrderSideEffect` for opening WhatsApp, `ProductUiState` for product CRUD feedback) — don't reuse the global `UiEvent` for these.

**CreateOrderViewModel scoping:** `CreateOrderViewModel` is scoped to the `CreateOrder` nav back-stack entry so that `MainScreen` and `CreateOrderScreen` share the same instance. Access it via `hiltViewModel(navBackStackEntry)`.

**Settings:** `SettingsRepository` uses `SharedPreferences` (not Room) for shop name, phone, and tax percentage, exposed as `StateFlow`.

**`Invoice` model:** `Invoice` in `model/` is a plain data class (no `@Entity`) used as a DTO when generating a WhatsApp order message — it is never persisted to the database. It is built from `Order` + `OrderItem` data at send time.

## Adding a New Screen

1. Add a `Screen` object in `Navigation.kt` (route, titleRes, icons)
2. Add a `composable(Screen.X.route)` in `PesaninNavHost`
3. Add a `TopAppBar` branch in `PesaninTopBar` if needed
4. Add the screen to `PesaninNavBar` list if it's a bottom-nav destination
5. Create the screen composable in `screen/`
6. Create a `@HiltViewModel` in `viewmodels/`

## Adding a New Entity

1. Create model class with `@Entity` in `model/`
2. Create DAO with `@Dao` in `dao/`
3. Add entity and DAO accessor to `AppDatabase`
4. Add `@Provides` bindings in `AppModule`
5. Create repository in `repository/`
6. Increment `version` in `AppDatabase` and add a Room `Migration`
