package com.royaal.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/* =====================================================================
 * RAW PALETTE
 * Монохром: только чёрный, белый и нейтральная серая шкала (R=G=B,
 * без оттенка). Единственное исключение — error/destructive, см. блок
 * STATUS ниже. Наружу палитра не отдаётся — семантические токены ниже
 * единственный публичный интерфейс.
 * =================================================================== */

private val black = Color(0xFF000000)
private val white = Color(0xFFFFFFFF)

// Neutral / grey — монохромная шкала, без изменений между темами
private val grey100 = Color(0xFFFBFBFB)
private val grey200 = Color(0xFFD2D2D2)
private val grey300 = Color(0xFFABABAB)
private val grey400 = Color(0xFF868686)
private val grey500 = Color(0xFF626262)
private val grey600 = Color(0xFF404040)
private val grey700 = Color(0xFF212121)

// Red — вне монохромной палитры. Сознательное единственное исключение
// для error/destructive, см. блок STATUS.
private val errorNormalLight = Color(0xFFB3261E)
private val errorPressedLight = Color(0xFF8C1D18)
private val errorDarkTone = Color(0xFFFFB4AB)
private val errorPressedDark = Color(0xFFF2A29A)

/* =====================================================================
 * BRAND
 * В монохроме тиры primary/secondary/tertiary различаются не оттенком,
 * а светлотой: primary — максимальный контраст (чёрный/белый), ниже —
 * серые ступени.
 * =================================================================== */

@Immutable
data class BrandColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
)

val lightBrandColors = BrandColors(
    primary = black,
    onPrimary = white,
    primaryContainer = grey200,
    onPrimaryContainer = black,
    secondary = grey600,
    onSecondary = white,
    secondaryContainer = grey200,
    onSecondaryContainer = grey700,
    tertiary = grey500,
    onTertiary = white,
    tertiaryContainer = grey100,
    onTertiaryContainer = grey700,
)

val darkBrandColors = BrandColors(
    primary = white,
    onPrimary = black,
    primaryContainer = grey600,
    onPrimaryContainer = white,
    secondary = grey300,
    onSecondary = black,
    secondaryContainer = grey600,
    onSecondaryContainer = grey100,
    tertiary = grey400,
    onTertiary = black,
    tertiaryContainer = grey700,
    onTertiaryContainer = grey100,
)

/* =====================================================================
 * BACKGROUND
 * Фон экрана + уровни подложек. Плоский минимализм: background и
 * surface — один и тот же чистый белый/чёрный, разделение уровней идёт
 * через границы и surfaceVariant, а не через оттенок.
 * =================================================================== */

@Immutable
data class BackgroundColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceElevated: Color,
    val scrim: Color,
) {
    /** Подложка карточки/секции — синоним surface, оставлен для читаемости вызовов. */
    val containerBackground: Color get() = surface
}

val lightBackgroundColors = BackgroundColors(
    background = white,
    onBackground = black,
    surface = white,
    onSurface = black,
    surfaceVariant = grey100,
    onSurfaceVariant = grey600,
    surfaceElevated = white,
    scrim = Color(0x99000000),
)

val darkBackgroundColors = BackgroundColors(
    background = black,
    onBackground = white,
    surface = black,
    onSurface = white,
    surfaceVariant = grey700,
    onSurfaceVariant = grey300,
    surfaceElevated = grey700,
    scrim = Color(0xCC000000),
)

/* =====================================================================
 * BUTTON
 * Один вариант = заливка + контент + рамка. Disabled лежит внутри
 * варианта, чтобы вызывающий код не собирал состояние вручную.
 * Filled — сплошной чёрный/белый, outlined — светлая 1px рамка без
 * заливки: основной графический приём монохромных брендов вроде
 * Pollariser.
 * =================================================================== */

@Immutable
data class ButtonVariantColors(
    val container: Color,
    val content: Color,
    val border: Color = Color.Transparent,
    val pressedContainer: Color = container,
    val disabledContainer: Color,
    val disabledContent: Color,
)

@Immutable
data class ButtonColors(
    /** Основное действие — сплошная заливка акцентом. */
    val filled: ButtonVariantColors,
    /** Вторичное действие — мягкая подложка. */
    val tonal: ButtonVariantColors,
    /** Третичное действие — только рамка. */
    val outlined: ButtonVariantColors,
    /** Наименее заметное — без фона и рамки. */
    val text: ButtonVariantColors,
    /** Деструктивное действие. */
    val destructive: ButtonVariantColors,
    /** Плоская кнопка */
    val flattened: ButtonVariantColors,
)

val lightButtonColors = ButtonColors(
    filled = ButtonVariantColors(
        container = black,
        content = white,
        pressedContainer = grey600,
        disabledContainer = grey200,
        disabledContent = grey400,
    ),
    tonal = ButtonVariantColors(
        container = grey200,
        content = black,
        pressedContainer = grey300,
        disabledContainer = grey100,
        disabledContent = grey400,
    ),
    outlined = ButtonVariantColors(
        container = white,
        content = black,
        // Как у карточек (brandColors.secondaryContainer): кнопка не должна быть контрастнее их.
        border = grey200,
        pressedContainer = grey100,
        disabledContainer = Color.Transparent,
        disabledContent = grey300,
    ),
    text = ButtonVariantColors(
        container = Color.Transparent,
        content = black,
        pressedContainer = grey100,
        disabledContainer = Color.Transparent,
        disabledContent = grey300,
    ),
    destructive = ButtonVariantColors(
        container = errorNormalLight,
        content = white,
        pressedContainer = errorPressedLight,
        disabledContainer = grey200,
        disabledContent = grey400,
    ),
    flattened = ButtonVariantColors(
        container = Color.Transparent,
        content = black,
        border = Color.Transparent,
        pressedContainer = grey300,
        disabledContainer = Color.Transparent,
        disabledContent = grey400
    )
)

val darkButtonColors = ButtonColors(
    filled = ButtonVariantColors(
        container = white,
        content = black,
        pressedContainer = grey300,
        disabledContainer = grey600,
        disabledContent = grey400,
    ),
    tonal = ButtonVariantColors(
        container = grey600,
        content = white,
        pressedContainer = grey500,
        disabledContainer = grey700,
        disabledContent = grey400,
    ),
    outlined = ButtonVariantColors(
        container = black,
        content = white,
        border = grey600,
        pressedContainer = grey700,
        disabledContainer = Color.Transparent,
        disabledContent = grey500,
    ),
    text = ButtonVariantColors(
        container = Color.Transparent,
        content = white,
        pressedContainer = grey700,
        disabledContainer = Color.Transparent,
        disabledContent = grey500,
    ),
    destructive = ButtonVariantColors(
        container = errorDarkTone,
        content = black,
        pressedContainer = errorPressedDark,
        disabledContainer = grey600,
        disabledContent = grey400,
    ),
    flattened = ButtonVariantColors(
        container = black,
        content = white,
        border = black,
        pressedContainer = grey200,
        disabledContainer = grey400,
        disabledContent = grey600,
    )
)

/* =====================================================================
 * SWITCH
 * =================================================================== */

@Immutable
data class SwitchColorsVariant(
    val checkedContainer: Color,
    val uncheckedContainer: Color,
    val checkedThumb: Color,
    val uncheckedThumb: Color,
    val checkedBorder: Color,
    val uncheckedBorder: Color,
)

@Immutable
data class SwitchColors(
    val enabled: SwitchColorsVariant,
    val disabled: SwitchColorsVariant,
)

internal val lightSwitchColors = SwitchColors(
    enabled = SwitchColorsVariant(
        checkedContainer = black,
        uncheckedContainer = white,
        checkedThumb = white,
        uncheckedThumb = grey300,
        checkedBorder = black,
        uncheckedBorder = grey300,
    ),
    disabled = SwitchColorsVariant(
        checkedContainer = grey200,
        uncheckedContainer = grey200,
        checkedThumb = grey300,
        uncheckedThumb = grey300,
        checkedBorder = grey200,
        uncheckedBorder = grey200,
    )
)

internal val darkSwitchColors = SwitchColors(
    enabled = SwitchColorsVariant(
        checkedContainer = white,
        uncheckedContainer = black,
        checkedThumb = black,
        uncheckedThumb = grey400,
        checkedBorder = grey400,
        uncheckedBorder = grey400,
    ),
    disabled = SwitchColorsVariant(
        checkedContainer = grey400,
        uncheckedContainer = grey400,
        checkedThumb = grey300,
        uncheckedThumb = grey300,
        checkedBorder = grey400,
        uncheckedBorder = grey400,
    )
)

/* =====================================================================
 * CHECKBOX
 * =================================================================== */

@Immutable
data class CheckboxColorsVariant(
    val checkedContainer: Color,
    val uncheckedContainer: Color,
    val checkedBorder: Color,
    val uncheckedBorder: Color,
    val contentColor: Color,
)

@Immutable
data class CheckboxColors(
    val enabled: CheckboxColorsVariant,
    val disabled: CheckboxColorsVariant,
    val important: CheckboxColorsVariant,
)

internal val lightCheckboxColors = CheckboxColors(
    enabled = CheckboxColorsVariant(
        checkedContainer = black,
        uncheckedContainer = white,
        checkedBorder = black,
        uncheckedBorder = grey300,
        contentColor = white
    ),
    disabled = CheckboxColorsVariant(
        checkedContainer = grey300,
        uncheckedContainer = grey200,
        checkedBorder = grey300,
        uncheckedBorder = grey300,
        contentColor = white,
    ),
    important = CheckboxColorsVariant(
        checkedContainer = black,
        uncheckedContainer = white,
        checkedBorder = black,
        uncheckedBorder = errorPressedLight,
        contentColor = white,
    )
)

internal val darkCheckboxColors = CheckboxColors(
    enabled = CheckboxColorsVariant(
        checkedContainer = white,
        uncheckedContainer = black,
        checkedBorder = white,
        uncheckedBorder = grey300,
        contentColor = black
    ),
    disabled = CheckboxColorsVariant(
        checkedContainer = grey300,
        uncheckedContainer = grey400,
        checkedBorder = grey300,
        uncheckedBorder = grey300,
        contentColor = black,
    ),
    important = CheckboxColorsVariant(
        checkedContainer = white,
        uncheckedContainer = black,
        checkedBorder = white,
        uncheckedBorder = errorPressedDark,
        contentColor = black,
    ),
)

/* =====================================================================
 * ICON
 * =================================================================== */

@Immutable
data class InputColors(
    val container: Color,
    val disabledContainer: Color,
    val border: Color,
    val disabledBorder: Color,
    val errorBorder: Color,
    val content: Color,
    val subContent: Color,
    val disabledContent: Color,
    val errorContent: Color,
)

internal val lightInputColors = InputColors(
    container = white,
    disabledContainer = grey200,
    border = grey200,
    disabledBorder = grey200,
    errorBorder = errorNormalLight,
    content = black,
    subContent = grey400,
    disabledContent = grey300,
    errorContent = errorNormalLight,
)

internal val darkInputColors = InputColors(
    container = black,
    disabledContainer = grey400,
    border = grey400,
    disabledBorder = grey400,
    errorBorder = errorDark,
    content = white,
    subContent = grey200,
    disabledContent = grey300,
    errorContent = errorDark,
)

/* =====================================================================
 * DROPDOWN
 * =================================================================== */

@Immutable
data class DropdownColors(
    val container: Color,
    val disabledContainer: Color,
    val border: Color,
    val openBorder: Color,
    val disabledBorder: Color,
    val content: Color,
    val placeholder: Color,
    val disabledContent: Color,
    val menuContainer: Color,
    val menuBorder: Color,
)

internal val lightDropdownColors = DropdownColors(
    container = white,
    disabledContainer = grey200,
    border = grey400,
    openBorder = black,
    disabledBorder = grey300,
    content = black,
    placeholder = grey500,
    disabledContent = grey300,
    menuContainer = white,
    menuBorder = grey200,
)

internal val darkDropdownColors = DropdownColors(
    container = grey700,
    disabledContainer = grey600,
    border = grey400,
    openBorder = white,
    disabledBorder = grey500,
    content = white,
    placeholder = grey300,
    disabledContent = grey500,
    menuContainer = grey700,
    menuBorder = grey600,
)


/* =====================================================================
 * ICON
 * =================================================================== */

@Immutable
data class IconColors(
    val primary: Color,
    val secondary: Color,
    val disabled: Color,
    /** Иконка поверх сплошной акцентной заливки. */
    val onAccent: Color,
    /** Иконка, которая сама является акцентом: активный таб, выбранный пункт. */
    val accent: Color,
    /** Иконка внутри мягкого контейнера (чип, бейдж). */
    val onContainer: Color,
)

val lightIconColors = IconColors(
    primary = black,
    secondary = grey500,
    disabled = grey300,
    onAccent = white,
    accent = black,
    onContainer = black,
)

val darkIconColors = IconColors(
    primary = white,
    secondary = grey300,
    disabled = grey500,
    onAccent = black,
    accent = white,
    onContainer = white,
)

/* =====================================================================
 * TEXT
 * =================================================================== */

@Immutable
data class TextColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val disabled: Color,
    /** Текст поверх сплошной акцентной заливки. */
    val onAccent: Color,
    val accent: Color,
)

val lightTextColors = TextColors(
    primary = black,
    secondary = grey500,
    tertiary = grey400,
    disabled = grey300,
    onAccent = white,
    accent = black,
)

val darkTextColors = TextColors(
    primary = white,
    secondary = grey300,
    tertiary = grey400,
    disabled = grey500,
    onAccent = black,
    accent = white,
)

/* =====================================================================
 * LINK
 * Монохром: ссылка не красится в акцент, а остаётся основным текстовым
 * цветом и подчёркивается (TextSource уже ставит TextDecoration. Underline
 * для HTML-ссылок) — различие через вес/подчёркивание, не через оттенок.
 * =================================================================== */

@Immutable
data class LinkColors(
    val linkColor: Color,
    val visitedLinkColor: Color,
    val pressedLinkColor: Color,
)

val lightLinkColors = LinkColors(
    linkColor = black,
    visitedLinkColor = grey500,
    pressedLinkColor = grey600,
)

val darkLinkColors = LinkColors(
    linkColor = white,
    visitedLinkColor = grey300,
    pressedLinkColor = grey400,
)

/* =====================================================================
 * BORDER
 * Рамки, разделители, обводка полей ввода.
 * =================================================================== */

@Immutable
data class BorderColors(
    val default: Color,
    val subtle: Color,
    val strong: Color,
    /** Рамка поля ввода в фокусе. */
    val focused: Color,
)

val lightBorderColors = BorderColors(
    default = grey300,
    subtle = grey200,
    strong = black,
    focused = black,
)

val darkBorderColors = BorderColors(
    default = grey500,
    subtle = grey600,
    strong = white,
    focused = white,
)

/* =====================================================================
 * FILTER CHIP
 * =================================================================== */

@Immutable
data class FilterChipColors(
    val container: Color,
    val pressedContainer: Color,
    val selectedContainer: Color,
    val disabledContainer: Color,
    val border: Color,
    val selectedBorder: Color,
    val disabledBorder: Color,
    val content: Color,
    val selectedContent: Color,
    val disabledContent: Color,
    val count: Color,
    val selectedCount: Color,
)

internal val lightFilterChipColors = FilterChipColors(
    container = white,
    pressedContainer = grey200,
    selectedContainer = black,
    disabledContainer = grey200,
    border = grey400,
    selectedBorder = black,
    disabledBorder = grey300,
    content = black,
    selectedContent = white,
    disabledContent = grey300,
    count = grey500,
    selectedCount = white,
)

internal val darkFilterChipColors = FilterChipColors(
    container = grey700,
    pressedContainer = grey600,
    selectedContainer = white,
    disabledContainer = grey600,
    border = grey400,
    selectedBorder = white,
    disabledBorder = grey500,
    content = white,
    selectedContent = black,
    disabledContent = grey500,
    count = grey300,
    selectedCount = black,
)

/* =====================================================================
 * TAG
 * =================================================================== */

@Immutable
data class TagColorsVariant(
    val container: Color,
    val border: Color,
    val content: Color,
)

@Immutable
data class TagColors(
    val neutral: TagColorsVariant,
    val outline: TagColorsVariant,
    val solid: TagColorsVariant,
    val error: TagColorsVariant,
)

internal val lightTagColors = TagColors(
    neutral = TagColorsVariant(
        container = grey200,
        border = Color.Transparent,
        content = black,
    ),
    outline = TagColorsVariant(
        container = Color.Transparent,
        border = grey400,
        content = black,
    ),
    solid = TagColorsVariant(
        container = black,
        border = Color.Transparent,
        content = white,
    ),
    error = TagColorsVariant(
        container = Color.Transparent,
        border = errorNormalLight,
        content = errorNormalLight,
    ),
)

internal val darkTagColors = TagColors(
    neutral = TagColorsVariant(
        container = grey600,
        border = Color.Transparent,
        content = white,
    ),
    outline = TagColorsVariant(
        container = Color.Transparent,
        border = grey400,
        content = white,
    ),
    solid = TagColorsVariant(
        container = white,
        border = Color.Transparent,
        content = black,
    ),
    error = TagColorsVariant(
        container = Color.Transparent,
        border = errorDarkTone,
        content = errorDarkTone,
    ),
)

/* =====================================================================
 * PROGRESS BAR
 * =================================================================== */

@Immutable
data class ProgressBarColors(
    val track: Color,
    val fill: Color,
    val errorFill: Color,
    val label: Color,
    val value: Color,
    val errorValue: Color,
)

internal val lightProgressBarColors = ProgressBarColors(
    track = grey200,
    fill = black,
    errorFill = errorNormalLight,
    label = black,
    value = grey500,
    errorValue = errorNormalLight,
)

internal val darkProgressBarColors = ProgressBarColors(
    track = grey600,
    fill = white,
    errorFill = errorDarkTone,
    label = white,
    value = grey300,
    errorValue = errorDarkTone,
)


/* =====================================================================
 * BOTTOM SHEET
 * =================================================================== */

/* =====================================================================
 * SKELETON
 * Заглушка на месте загружающихся данных: [base] — сама форма,
 * [highlight] — пробегающий по ней блик.
 * =================================================================== */

@Immutable
data class SkeletonColors(
    val base: Color,
    val highlight: Color,
)

internal val lightSkeletonColors = SkeletonColors(
    base = grey200,
    highlight = grey100,
)

internal val darkSkeletonColors = SkeletonColors(
    base = grey700,
    highlight = grey600,
)

@Immutable
data class BottomNavBarColors(
    val background: Color,
    val selectedBackground: Color,
    val selectedContent: Color,
    val content: Color,
)

internal val lightBottomNavBarColors = BottomNavBarColors(
    background = white,
    selectedBackground = black,
    selectedContent = white,
    content = black,
)

internal val darkBottomNavBarColors = BottomNavBarColors(
    background = black,
    selectedBackground = white,
    selectedContent = black,
    content = white,
)

/* =====================================================================
 * BOTTOM SHEET
 * =================================================================== */

@Immutable
data class BottomSheetColors(
    val container: Color,
    val scrim: Color,
    val handle: Color,
    val content: Color,
)

internal val lightBottomSheetColors = BottomSheetColors(
    container = white,
    scrim = Color(0x66000000),
    handle = grey400,
    content = black,
)

internal val darkBottomSheetColors = BottomSheetColors(
    container = grey700,
    scrim = Color(0xA3000000),
    handle = grey400,
    content = white,
)


/* =====================================================================
 * SNACKBAR
 * =================================================================== */

@Immutable
data class SnackbarColors(
    /** Primary — чёрный в светлой теме, белый в тёмной */
    val container: Color,
    /** On-primary — текст сообщения, текст и рамка кнопки действия */
    val content: Color,
)

internal val lightSnackbarColors = SnackbarColors(
    container = black,
    content = white,
)

internal val darkSnackbarColors = SnackbarColors(
    container = white,
    content = black,
)

/* =====================================================================
 * DIALOG
 * =================================================================== */

@Immutable
data class DialogColors(
    val container: Color,
    val title: Color,
    val body: Color,
)

internal val lightDialogColors = DialogColors(
    container = white,
    title = black,
    body = grey500,
)

internal val darkDialogColors = DialogColors(
    container = grey700,
    title = white,
    body = grey300,
)

/* =====================================================================
 * ACCESSORS
 * Extension-свойства, чтобы не править AppTheme в Theme.kt.
 * Использование: AppTheme.buttonColors.filled.container
 * =================================================================== */

// linkColors и backgroundColor объявлены членами object AppTheme в Theme.kt
// и работают как есть — здесь их дублировать нельзя, член всегда перекрывает
// extension. Если захочешь перенести всё в один файл, удали их из Theme.kt.

val AppTheme.brandColors: BrandColors
    @Composable get() = if (isSystemInDarkTheme()) darkBrandColors else lightBrandColors

val AppTheme.iconColors: IconColors
    @Composable get() = if (isSystemInDarkTheme()) darkIconColors else lightIconColors

val AppTheme.textColors: TextColors
    @Composable get() = if (isSystemInDarkTheme()) darkTextColors else lightTextColors

val AppTheme.backgroundColors
    @Composable get() = if (isSystemInDarkTheme()) darkBackgroundColors else lightBackgroundColors

internal val AppTheme.linkColors: LinkColors
    @Composable get() = if (isSystemInDarkTheme()) darkLinkColors else lightLinkColors

internal val AppTheme.buttonColors: ButtonColors
    @Composable get() = if (isSystemInDarkTheme()) darkButtonColors else lightButtonColors

internal val AppTheme.switchColors: SwitchColors
    @Composable get() = if (isSystemInDarkTheme()) darkSwitchColors else lightSwitchColors

internal val AppTheme.checkboxColors: CheckboxColors
    @Composable get() = if (isSystemInDarkTheme()) darkCheckboxColors else lightCheckboxColors

internal val AppTheme.inputColors: InputColors
    @Composable get() = if (isSystemInDarkTheme()) darkInputColors else lightInputColors

internal val AppTheme.dropdownColors: DropdownColors
    @Composable get() = if (isSystemInDarkTheme()) darkDropdownColors else lightDropdownColors

internal val AppTheme.filterChipColors: FilterChipColors
    @Composable get() = if (isSystemInDarkTheme()) darkFilterChipColors else lightFilterChipColors

internal val AppTheme.tagColors: TagColors
    @Composable get() = if (isSystemInDarkTheme()) darkTagColors else lightTagColors

internal val AppTheme.progressBarColors: ProgressBarColors
    @Composable get() = if (isSystemInDarkTheme()) darkProgressBarColors else lightProgressBarColors

internal val AppTheme.bottomSheetColors: BottomSheetColors
    @Composable get() = if (isSystemInDarkTheme()) darkBottomSheetColors else lightBottomSheetColors

internal val AppTheme.skeletonColors: SkeletonColors
    @Composable get() = if (isSystemInDarkTheme()) darkSkeletonColors else lightSkeletonColors

internal val AppTheme.bottomNavBarColors: BottomNavBarColors
    @Composable get() = if (isSystemInDarkTheme()) darkBottomNavBarColors else lightBottomNavBarColors

internal val AppTheme.snackbarColors: SnackbarColors
    @Composable get() = if (isSystemInDarkTheme()) darkSnackbarColors else lightSnackbarColors

internal val AppTheme.dialogColors: DialogColors
    @Composable get() = if (isSystemInDarkTheme()) darkDialogColors else lightDialogColors

