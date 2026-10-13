package com.royaal.designsystem.components.dropdown

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.DropdownColors
import com.royaal.designsystem.theme.dropdownColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.pressScale

// Размеры из GGData: Input 48dp / radius-12 / space-16, меню space-4 ниже, elevation-6.
private val TriggerHeight = 48.dp
private val CornerRadiusDp = 12.dp
private val HorizontalPadding = 16.dp
private val TitleSpacing = 4.dp
private val ContentSpacing = 8.dp
private val BorderWidth = 1.dp
private val OpenBorderWidth = 2.dp
private val ChevronSize = 20.dp
private val CheckSize = 20.dp
private val ItemHeight = 48.dp
private val MenuGap = 4.dp
private val MenuElevation = AppConst.highShadowElevationDp
private val MenuMaxHeight = ItemHeight * 7

/** Запас вокруг меню внутри окна Popup, чтобы тень не обрезалась краем окна. */
private val MenuShadowPadding = 8.dp

private val menuShape = RoundedCornerShape(CornerRadiusDp)

private const val AnimationDurationMs = 128
private val colorAnimationSpec: FiniteAnimationSpec<Color> = tween(AnimationDurationMs, easing = EaseOut)
private val floatAnimationSpec: FiniteAnimationSpec<Float> = tween(AnimationDurationMs, easing = EaseOut)

private const val MenuInitialScale = 0.98f

@Immutable
data class DropdownOption<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

/**
 * Выбор одного значения из короткого списка (примерно 3–10 пунктов).
 * Для длинных списков в GGData используется BottomSheet.
 *
 * @param value выбранное значение; null — ничего не выбрано, показывается [hint].
 */
@Composable
fun <T> GGDropdown(
    options: List<DropdownOption<T>>,
    value: T?,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    hint: String? = null,
    enabled: Boolean = true,
) {
    val colors = AppTheme.dropdownColors
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) expanded = false
    }
    val isOpen = expanded && enabled
    val selectedOption = options.firstOrNull { it.value == value }

    // Один прогресс на рамку и шеврон; всё читается в draw-фазе.
    val openProgress by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = floatAnimationSpec,
        label = "DropdownOpen",
    )
    val containerColor by animateColorAsState(
        targetValue = if (enabled) colors.container else colors.disabledContainer,
        animationSpec = colorAnimationSpec,
        label = "DropdownContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = if (enabled) colors.border else colors.disabledBorder,
        animationSpec = colorAnimationSpec,
        label = "DropdownBorder",
    )
    // Цвет текста без анимации: Text читает его в композиции.
    val contentColor = if (enabled) colors.content else colors.disabledContent
    val valueColor = when {
        !enabled -> colors.disabledContent
        selectedOption == null -> colors.placeholder
        else -> colors.content
    }

    var triggerWidthPx by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TitleSpacing),
    ) {
        title?.let { title ->
            Text(
                text = title,
                style = AppTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Box — якорь для Popup: меню позиционируется относительно его границ.
        Box {
            val triggerInteractionSource = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .pressScale(triggerInteractionSource, enabled)
                    .fillMaxWidth()
                    .height(TriggerHeight)
                    .onSizeChanged { triggerWidthPx = it.width }
                    .drawBehind {
                        val radius = CornerRadiusDp.toPx()
                        drawRoundRect(color = containerColor, cornerRadius = CornerRadius(radius))

                        val borderPx = BorderWidth.toPx() +
                                (OpenBorderWidth.toPx() - BorderWidth.toPx()) * openProgress
                        drawRoundRect(
                            color = lerp(borderColor, colors.openBorder, openProgress),
                            topLeft = Offset(borderPx / 2f, borderPx / 2f),
                            size = Size(size.width - borderPx, size.height - borderPx),
                            cornerRadius = CornerRadius((radius - borderPx / 2f).coerceAtLeast(0f)),
                            style = Stroke(width = borderPx),
                        )
                    }
                    .clickable(
                        interactionSource = triggerInteractionSource,
                        indication = null,
                        enabled = enabled,
                        role = Role.DropdownList,
                        onClick = { expanded = !expanded },
                    )
                    .padding(horizontal = HorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ContentSpacing),
            ) {
                Text(
                    text = selectedOption?.label ?: hint.orEmpty(),
                    style = AppTheme.typography.bodyMedium,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Chevron(color = contentColor, openProgress = { openProgress })
            }

            DropdownMenuPopup(
                isOpen = isOpen,
                widthPx = triggerWidthPx,
                options = options,
                selectedValue = value,
                colors = colors,
                onDismissRequest = { expanded = false },
                onSelect = { option ->
                    expanded = false
                    if (option.value != value) onValueChange(option.value)
                },
            )
        }
    }
}

@Composable
private fun <T> DropdownMenuPopup(
    isOpen: Boolean,
    widthPx: Int,
    options: List<DropdownOption<T>>,
    selectedValue: T?,
    colors: DropdownColors,
    onDismissRequest: () -> Unit,
    onSelect: (DropdownOption<T>) -> Unit,
) {
    // Popup держится в композиции, пока не доиграет анимация закрытия.
    val menuState = remember { MutableTransitionState(false) }
    menuState.targetState = isOpen
    if (!menuState.currentState && !menuState.targetState) return

    val density = LocalDensity.current
    val gapPx = with(density) { MenuGap.roundToPx() }
    val shadowPaddingPx = with(density) { MenuShadowPadding.roundToPx() }
    val positionProvider = remember(gapPx, shadowPaddingPx) {
        DropdownMenuPositionProvider(gapPx = gapPx, shadowPaddingPx = shadowPaddingPx)
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        val transition = rememberTransition(menuState, label = "DropdownMenu")
        val alpha by transition.animateFloat(
            transitionSpec = { floatAnimationSpec },
            label = "DropdownMenuAlpha",
        ) { open -> if (open) 1f else 0f }
        val scale by transition.animateFloat(
            transitionSpec = { floatAnimationSpec },
            label = "DropdownMenuScale",
        ) { open -> if (open) 1f else MenuInitialScale }

        Box(modifier = Modifier.padding(MenuShadowPadding)) {
            Column(
                modifier = Modifier
                    .width(with(density) { widthPx.toDp() })
                    .graphicsLayer {
                        this.alpha = alpha
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        shadowElevation = MenuElevation.toPx()
                        shape = menuShape
                        clip = true
                    }
                    .background(colors.menuContainer)
                    .border(BorderWidth, colors.menuBorder, menuShape)
                    .heightIn(max = MenuMaxHeight)
                    .verticalScroll(rememberScrollState()),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        option = option,
                        selected = option.value == selectedValue,
                        colors = colors,
                        onClick = { onSelect(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> DropdownMenuItem(
    option: DropdownOption<T>,
    selected: Boolean,
    colors: DropdownColors,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ItemHeight)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = null,
                enabled = option.enabled,
                onClick = onClick,
            )
            .padding(horizontal = HorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ContentSpacing),
    ) {
        Text(
            text = option.label,
            style = AppTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Medium else null,
            color = if (option.enabled) colors.content else colors.disabledContent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = AppIcons.check,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(CheckSize),
            )
        }
    }
}

/** Шеврон по правилам иконок GGData: обводка 1.5 на сетке 24, круглые концы. */
@Composable
private fun Chevron(color: Color, openProgress: () -> Float) {
    Canvas(
        modifier = Modifier
            .size(ChevronSize)
            .graphicsLayer { rotationZ = 180f * openProgress() },
    ) {
        val unit = size.width / 24f
        val path = Path().apply {
            moveTo(6f * unit, 9f * unit)
            lineTo(12f * unit, 15f * unit)
            lineTo(18f * unit, 9f * unit)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 1.5f * unit,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/**
 * Меню под триггером на расстоянии [gapPx]; если снизу не помещается — над ним.
 * [shadowPaddingPx] — отступ вокруг меню внутри Popup, его вычитаем из позиции.
 */
private class DropdownMenuPositionProvider(
    private val gapPx: Int,
    private val shadowPaddingPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (anchorBounds.left - shadowPaddingPx).coerceIn(0, maxX)

        val below = anchorBounds.bottom + gapPx - shadowPaddingPx
        val above = anchorBounds.top - gapPx - popupContentSize.height + shadowPaddingPx
        val fitsBelow = below + popupContentSize.height <= windowSize.height
        val y = if (fitsBelow || above < 0) below else above

        return IntOffset(x, y)
    }
}

@Preview(showBackground = true)
@Composable
private fun GGDropdownPreview() = AppTheme {
    val options = listOf(
        DropdownOption("relevance", "Relevance"),
        DropdownOption("release", "Release date"),
        DropdownOption("rating", "Rating"),
        DropdownOption("price", "Price", enabled = false),
    )
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        var sort by remember { mutableStateOf<String?>("relevance") }
        GGDropdown(
            options = options,
            value = sort,
            onValueChange = { sort = it },
            title = "Sort by",
        )
        GGDropdown(
            options = options,
            value = null,
            onValueChange = {},
            title = "Platform",
            hint = "Select",
        )
        GGDropdown(
            options = options,
            value = "rating",
            onValueChange = {},
            title = "Sort by",
            enabled = false,
        )
    }
}