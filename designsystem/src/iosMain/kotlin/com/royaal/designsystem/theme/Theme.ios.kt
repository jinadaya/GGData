package com.royaal.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

// No Material You / wallpaper-accent concept on iOS.
@Composable
actual fun dynamicColorScheme(darkTheme: Boolean): ColorScheme? = null
