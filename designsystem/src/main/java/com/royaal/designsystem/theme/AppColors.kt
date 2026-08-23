package com.royaal.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val grey100 = Color(0xFFFBFBFB)
private val grey200 = Color(0xFFD2D2D2)
private val grey300 = Color(0xFFABABAB)
private val grey400 = Color(0xFF868686)
private val grey500 = Color(0xFF626262)
private val grey600 = Color(0xFF404040)
private val grey700 = Color(0xFF212121)

// Secondary scale (cyan)
private val secondary100 = Color(0xFFE8F9FF)
private val secondary200 = Color(0xFF41DFFF)
private val secondary500 = Color(0xFF006778)
private val secondary700 = Color(0xFF002228)

// Tertiary scale (blue)
private val tertiary100 = Color(0xFFC4D9FF)
private val tertiary700 = Color(0xFF001225)

// Quaternary scale (indigo)
private val quaternary100 = Color(0xFFECF0FF)
private val quaternary300 = Color(0xFF82A8FF)
private val quaternary400 = Color(0xFF0F81FF)
private val quaternary600 = Color(0xFF003D81)
private val quaternary700 = Color(0xFF001F48)

data class LinkColors(
    val linkColor: Color,
)

private val darkLinkColor = Color(0xFF006874)
private val lightLinkColor = Color(0xFF82d3e1)

val darkLinkColors = LinkColors(linkColor = darkLinkColor)
val lightLinkColors = LinkColors(linkColor = lightLinkColor)

data class BackgroundColors(
    val primaryBackground: Color,
    val secondaryBackground: Color,
) {
    val containerBackground = secondaryBackground
}

val lightBackgroundColors = BackgroundColors(
    primaryBackground = grey100,
    secondaryBackground = secondary100,
)

val darkBackgroundColors = BackgroundColors(
    primaryBackground = grey700,
    secondaryBackground = secondary700,
)

data class SurfaceColors(
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
)

val lightSurfaceColors = SurfaceColors(
    surfaceContainerLowest = grey100,
    surfaceContainerLow = grey200,
    surfaceContainer = grey300,
    surfaceContainerHigh = grey400,
    onSurface = grey700,
    onSurfaceVariant = grey500,
)

val darkSurfaceColors = SurfaceColors(
    surfaceContainerLowest = grey700,
    surfaceContainerLow = grey600,
    surfaceContainer = grey500,
    surfaceContainerHigh = grey400,
    onSurface = grey100,
    onSurfaceVariant = grey300,
)

data class ContainerColors(
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val quaternaryContainer: Color,
    val onQuaternaryContainer: Color,
)

val lightContainerColors = ContainerColors(
    secondaryContainer = secondary100,
    onSecondaryContainer = secondary700,
    tertiaryContainer = tertiary100,
    onTertiaryContainer = tertiary700,
    quaternaryContainer = quaternary100,
    onQuaternaryContainer = quaternary700,
)

val darkContainerColors = ContainerColors(
    secondaryContainer = secondary700,
    onSecondaryContainer = secondary100,
    tertiaryContainer = tertiary700,
    onTertiaryContainer = tertiary100,
    quaternaryContainer = quaternary700,
    onQuaternaryContainer = quaternary100,
)

data class QuaternaryColors(
    val quaternary: Color,
    val onQuaternary: Color,
    val quaternaryContainer: Color,
    val onQuaternaryContainer: Color,
)

val lightQuaternaryColors = QuaternaryColors(
    quaternary = quaternary400,
    onQuaternary = Color.White,
    quaternaryContainer = quaternary100,
    onQuaternaryContainer = quaternary700,
)

val darkQuaternaryColors = QuaternaryColors(
    quaternary = quaternary300,
    onQuaternary = quaternary700,
    quaternaryContainer = quaternary600,
    onQuaternaryContainer = quaternary100,
)

data class OutlineColors(
    val outline: Color,
    val outlineVariant: Color,
    val outlineBorder: Color,
)

val lightOutlineColors = OutlineColors(
    outline = grey400,
    outlineVariant = grey200,
    outlineBorder = quaternary400,
)

val darkOutlineColors = OutlineColors(
    outline = grey400,
    outlineVariant = grey600,
    outlineBorder = quaternary600,
)

data class TextColors(
    val primaryText: Color,
    val secondaryText: Color,
    val disabledText: Color,
)

val lightTextColors = TextColors(
    primaryText = grey700,
    secondaryText = grey500,
    disabledText = grey300,
)

val darkTextColors = TextColors(
    primaryText = grey100,
    secondaryText = grey300,
    disabledText = grey500,
)

data class IconColors(
    val iconPrimary: Color,
    val iconSecondary: Color,
    val iconDisabled: Color,
    val iconOnAccent: Color,
    val iconAccent: Color,
)

val lightIconColors = IconColors(
    iconPrimary = grey700,
    iconSecondary = grey500,
    iconDisabled = grey300,
    iconOnAccent = grey100,
    iconAccent = secondary500,
)

val darkIconColors = IconColors(
    iconPrimary = grey100,
    iconSecondary = grey300,
    iconDisabled = grey500,
    iconOnAccent = grey100,
    iconAccent = secondary200,
)

val AppTheme.linkColors
    @Composable get() = if (isSystemInDarkTheme()) darkLinkColors else lightLinkColors

val AppTheme.backgroundColor
    @Composable get() = if (isSystemInDarkTheme()) darkBackgroundColors else lightBackgroundColors

val AppTheme.surfaceColors: SurfaceColors
    @Composable get() = if (!isSystemInDarkTheme()) lightSurfaceColors else darkSurfaceColors

val AppTheme.containerColors: ContainerColors
    @Composable get() = if (!isSystemInDarkTheme()) lightContainerColors else darkContainerColors

val AppTheme.quaternaryColors: QuaternaryColors
    @Composable get() = if (!isSystemInDarkTheme()) lightQuaternaryColors else darkQuaternaryColors

val AppTheme.outlineColors: OutlineColors
    @Composable get() = if (!isSystemInDarkTheme()) lightOutlineColors else darkOutlineColors

val AppTheme.textColors: TextColors
    @Composable get() = if (!isSystemInDarkTheme()) lightTextColors else darkTextColors

val AppTheme.iconColors: IconColors
    @Composable get() = if (!isSystemInDarkTheme()) lightIconColors else darkIconColors
