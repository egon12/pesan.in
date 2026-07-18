# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pesan.in is an Android ordering/inventory app (package `org.kotakwarna.pesanin`) built with Kotlin, Jetpack Compose, Room, and Hilt.

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
├── repository/    # Data layer — OrderRepository, ProductRepository, SettingsRepository
├── viewmodels/    # HiltViewModel classes; emit UiEvent via MutableSharedFlow
├── screen/        # Compose screens, Navigation.kt (Screen sealed class + NavHost)
├── util/          # ContextExt.kt (openWhatsApp helper)
└── ui/theme/      # Material 3 theme
```

**Navigation:** `Screen` is a sealed class in `Navigation.kt` defining all routes. `MainScreen` hosts a `Scaffold` with `PesaninNavHost`, `PesaninTopBar`, `PesaninNavBar`, and `PesaninFAB`. Navigation events flow through `MainViewModel.events: MutableSharedFlow<UiEvent>`.

The `orderDetail/{orderId}` route is a plain string route (not a `Screen` object) navigated to directly from `OrdersScreen` via `navController.navigate("orderDetail/${order.id}")`. `OrderDetailScreen` reads the orderId from `SavedStateHandle` via `OrderDetailViewModel`.

**ViewModel → UI event bus:** `MainViewModel` emits `UiEvent` (Navigate, NavigateBack, Snackbar) collected in `MainScreen` via `LaunchedEffect`. Screens call `viewModel.navigate(Screen.X)` or `viewModel.alert(msg)` rather than controlling the nav controller directly. Screen-specific side effects use their own sealed classes (e.g., `CreateOrderSideEffect` for opening WhatsApp, `ProductUiState` for product CRUD feedback, `OrderDetailUiState` for Loading/Success/Error) — don't reuse the global `UiEvent` for these.

**CreateOrderViewModel scoping:** `CreateOrderViewModel` is scoped to the `CreateOrder` nav back-stack entry so that `MainScreen` and `CreateOrderScreen` share the same instance. Access it via `hiltViewModel(navBackStackEntry)`.

**Settings:** `SettingsRepository` uses `SharedPreferences` (not Room) for shop name, phone, tax percentage, and country code (default `"+62"`), all exposed as `StateFlow`. The country code is used by `CreateOrderViewModel.normalizePhone()` to prepend the code when sending to WhatsApp: numbers starting with `+` are used as-is, numbers starting with `0` have the `0` replaced by the country code, otherwise the code is prepended.

**`Invoice` model:** `Invoice` in `model/` is a plain data class (no `@Entity`) used as a DTO when generating a WhatsApp order message — it is never persisted to the database. It is built from `Order` + `OrderItem` data at send time via `OrderRepository.generateInvoice()`.

**`Order` model:** `Order` has a `status: OrderStatus` field (enum: `PENDING`, `COMPLETED`, `CANCELED`). `Order.items: List<OrderItem>` is a non-persisted field populated at read time by `OrderRepository.getOrderWithItems()`. Status can be updated via `OrderRepository.updateOrderStatus()`.

**`CreateOrderViewModel`:** Exposes two save paths — `sendInvoice()` saves the order and opens WhatsApp (requires phone number); `saveOrder()` saves without WhatsApp (phone optional). `isSaveEnabled` requires both items and phone; `isSaveOnlyEnabled` requires only items.

**`ProductViewModel`:** `loadProduct(productId)` streams a product from the repo into `selectedProduct`. `updateProduct(productId, shortName, name, price)` preserves `shortName` on save.

**`CommonComponents.kt`:** Shared composables and utilities used across screens. Contains `CartFab` (the extended FAB showing cart item count and total) and the `formatter` (`DecimalFormat("Rp#,###.##")`) used for currency display throughout the app.

**`util/ContextExt.kt`:** `Context.openWhatsApp(phoneNumber, message)` launches the WhatsApp deep-link intent. Phone normalization happens in `CreateOrderViewModel` before calling this, not here.

## Screens

| Screen | Route | ViewModel | Notes |
|---|---|---|---|
| CreateOrderScreen | `createOrder` | `CreateOrderViewModel` | Start destination; scoped to back-stack entry |
| ProductListScreen | `products` | `ProductViewModel` | Tapping a card navigates to `EditProduct` |
| OrdersScreen | `orders` | `OrdersViewModel` | Tapping a card navigates to `orderDetail` |
| OrderDetailScreen | `orderDetail/{orderId}` | `OrderDetailViewModel` | Not a `Screen` object; uses plain route string |
| SettingsScreen | `settings` | `SettingsViewModel` | Includes country code field |
| ProductFormScreen | `product/create` | `ProductViewModel` | `Screen.CreateProduct`; clears form on success |
| ProductFormScreen | `product/edit/{productId}` | `ProductViewModel` | `Screen.EditProduct`; pops back on success |

## Adding a New Screen

1. Add a `Screen` object in `Navigation.kt` (route, titleRes, icons) — for routes with path params, define them in the `Screen` object's route string (e.g. `product/edit/{productId}`) and navigate with the filled-in string (e.g. `navController.navigate("product/edit/$id")`)
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
