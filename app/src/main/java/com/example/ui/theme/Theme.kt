package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ZvidDarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = NetflixTextPrimary,
    primaryContainer = NetflixDarkRed,
    onPrimaryContainer = NetflixTextPrimary,
    secondary = NetflixRed,
    onSecondary = NetflixTextPrimary,
    background = NetflixBlack,
    onBackground = NetflixTextPrimary,
    surface = NetflixDarkSurface,
    onSurface = NetflixTextPrimary,
    surfaceVariant = NetflixSurfaceVariant,
    onSurfaceVariant = NetflixTextSecondary,
    outline = NetflixBorder,
    outlineVariant = NetflixBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Zvid always uses premium Netflix-style Dark theme
    val colorScheme = ZvidDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NetflixBlack.toArgb()
                window.navigationBarColor = NetflixBlack.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
