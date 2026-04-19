# AGENTS.md

## Project Overview

Pesan.in is an Android ordering/inventory app built with Kotlin, Jetpack Compose, Room database, and Hilt for dependency injection.

## Build Commands

| Task | Command |
|------|---------|
| Build debug APK | `./gradlew assembleDebug` |
| Build release APK | `./gradlew assembleRelease` |
| Run tests | `./gradlew test` |
| Run unit tests | `./gradlew testDebugUnitTest` |
| Run instrumented tests | `./gradlew connectedDebugAndroidTest` |
| Clean build | `./gradlew clean` |

## Code Quality

| Task | Command |
|------|---------|
| Analyze code | `./gradlew analyze` |
| Check style | `./gradlew ktlintCheck` |
| Fix style | `./gradlew ktlintFormat` |

## Project Structure

```
app/src/main/java/org/egon12/pesanin/
├── core/           # Database & DI setup
├── dao/            # Room DAOs
├── model/          # Data models
├── repository/     # Data repositories
├── screen/         # Compose UI screens
├── viewmodels/     # ViewModels
└── ui/theme/       # Material theme
```

## Key Technologies

- **Kotlin** + **Jetpack Compose**
- **Room** for local database
- **Hilt** for dependency injection
- **Coroutines** for async operations
- **Material 3** design

## Common Tasks

### Adding a new entity
1. Create model class in `model/`
2. Create DAO in `dao/`
3. Add DAO to `AppDatabase`
4. Create repository in `repository/`
5. Add repository to `AppModule` (Hilt)
6. Create ViewModel in `viewmodels/`
7. Create Screen in `screen/`

### Database migration
1. Modify model class
2. Add migration in `AppDatabase`: `addMigrations(Migration(...))`
3. Increment `versionCode` in `build.gradle.kts`