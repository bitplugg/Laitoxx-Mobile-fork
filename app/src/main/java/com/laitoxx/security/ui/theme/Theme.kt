package com.laitoxx.security.ui.theme

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.laitoxx.security.utils.ThemeManager

private val DarkColorScheme = darkColorScheme(
    primary = AccentRed,
    secondary = LightRed,
    tertiary = Purple,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    error = LightRed,
    onError = White
)

@Composable
fun LAITOXXTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)

    val currentTheme by themeManager.currentTheme.collectAsState()

    // Создаем colorScheme на основе текущей темы
    val colorScheme = darkColorScheme(
        primary = currentTheme.hexToColor(currentTheme.accentPrimary),
        secondary = currentTheme.hexToColor(currentTheme.accentSecondary),
        tertiary = currentTheme.hexToColor(currentTheme.accentTertiary),
        background = currentTheme.hexToColor(currentTheme.backgroundPrimary),
        surface = currentTheme.hexToColor(currentTheme.backgroundSecondary),
        onPrimary = currentTheme.hexToColor(currentTheme.buttonText),
        onSecondary = currentTheme.hexToColor(currentTheme.textSecondary),
        onTertiary = currentTheme.hexToColor(currentTheme.textTertiary),
        onBackground = currentTheme.hexToColor(currentTheme.textPrimary),
        onSurface = currentTheme.hexToColor(currentTheme.textPrimary),
        error = currentTheme.hexToColor(currentTheme.errorColor),
        onError = currentTheme.hexToColor(currentTheme.buttonText),
        surfaceVariant = currentTheme.hexToColor(currentTheme.cardBackground),
        onSurfaceVariant = currentTheme.hexToColor(currentTheme.textSecondary)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            } catch (e: Exception) {
                Log.e("LAITOXXTheme", "Failed to set status bar color", e)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
