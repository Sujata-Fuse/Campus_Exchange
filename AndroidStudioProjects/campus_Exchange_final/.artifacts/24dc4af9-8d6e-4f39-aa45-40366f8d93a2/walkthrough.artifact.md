# Black Screen Startup Issue - Root Cause & Resolution Walkthrough

## Root Cause Analysis

1. **Unsafe Activity Cast in Theme Setup (`Theme.kt`)**:
   - `(view.context as Activity)` in `Campus_Exchange_finalTheme` threw a `ClassCastException` whenever Compose initialized with `view.context` wrapped in a `ContextThemeWrapper` or `TintContextWrapper`.
   - This unhandled runtime exception aborted Compose window rendering during Activity startup, resulting in a completely black screen on launch while Android Studio reported "Install successfully finished".

2. **Manual Tap Requirement on Splash Screen (`AppNavigation.kt`)**:
   - `ScreenRoute.SPLASH` lacked a `LaunchedEffect` timer to automatically transition out of the splash screen.
   - It relied entirely on manual touch events on an invisible overlay `Box`, leaving the app stuck on the initial launch state if not tapped.

3. **Eager & Unprotected Client Initialization (`SupabaseProvider.kt`)**:
   - Eager initialization of `SupabaseProvider.client` on object creation could throw uncaught `Throwable`s (such as `ExceptionInInitializerError` or `NoClassDefFoundError`) if token parsing or network configuration failed, bypassing `catch (e: Exception)` blocks in ViewModels and crashing composition.

---

## Applied Corrections

1. **`Theme.kt`**:
   - Replaced direct `(view.context as Activity)` cast with a safe recursive `Context.findActivity()` helper that unwraps `ContextWrapper` hierarchies.

2. **`AppNavigation.kt`**:
   - Added `LaunchedEffect(Unit)` to `ScreenRoute.SPLASH` that automatically advances to `HOME` (if authenticated) or `ONBOARDING_1` after 1.2 seconds, while retaining tap-to-skip.
   - Caught `Throwable` when checking `currentUserOrNull()`.

3. **`SupabaseProvider.kt` & ViewModels**:
   - Made `SupabaseProvider.client` `lazy` with a safe fallback catch block.
   - Updated ViewModel coroutines (`ProfileViewModel.kt`, etc.) to catch all `Throwable` instances (`catch (t: Throwable)`), preventing uncaught initializer errors from crashing Compose.

---

## Verification Results

- **Gradle Build**: `BUILD SUCCESSFUL` (`assembleDebug`)
- **Unit Tests**: PASSED (`app:testDebugUnitTest`)
