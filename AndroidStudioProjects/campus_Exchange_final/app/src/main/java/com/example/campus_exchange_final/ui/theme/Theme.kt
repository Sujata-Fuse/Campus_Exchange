package com.example.campus_exchange_final.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

private val LightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = PurpleSurface,
    onPrimaryContainer = BrandPurpleDark,
    secondary = TealFound,
    onSecondary = Color.White,
    secondaryContainer = TealSurface,
    tertiary = LightBlue,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = PurpleSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    error = CoralLost,
    onError = Color.White,
    errorContainer = CoralSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = BrandPurpleDark,
    onPrimaryContainer = Color.White,
    secondary = TealFound,
    onSecondary = Color.White,
    secondaryContainer = TealSurface,
    tertiary = LightBlue,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF404040),
    error = CoralLost,
    onError = Color.White,
    errorContainer = CoralSurface
)

@Composable
fun Campus_Exchange_finalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                activity.window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
