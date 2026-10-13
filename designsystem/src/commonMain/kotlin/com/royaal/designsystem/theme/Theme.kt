package com.royaal.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Platform's "wallpaper accent" color scheme (Material You on Android). Returns null
 * where the platform has no such concept (iOS) - callers fall back to [AppTheme]'s own scheme.
 */
@Composable
expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeContrast: AppThemeContrast = AppThemeContrast.NORMAL,
    content: @Composable() () -> Unit,
) {
    val colorScheme = (if (dynamicColor) dynamicColorScheme(darkTheme) else null)
        ?: when (themeContrast) {
            AppThemeContrast.NORMAL -> AppTheme.materialColors
            AppThemeContrast.MEDIUM -> AppTheme.mediumContrastColors
            AppThemeContrast.HIGH -> AppTheme.highContrastColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        content = content
    )
}
