package com.royaal.designsystem.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

internal object AppConst {
    /**
     * For flat items
     */
    val noShadowElevationDp = 0.dp

    /**
     * For base elements that are on flat surface
     */
    val baseShadowElevationDp = 2.dp

    /**
     * For elements that may be higher than base ones
     */
    val midShadowElevationDp = 4.dp

    /**
     * For elements that most likely animate down on click
     */
    val highShadowElevationDp = 6.dp

    /**
     * For flying components: dialog, dropdown, etc
     */
    val aboveShadowElevationDp = 8.dp

    /**
     * Height of every button, with or without an icon, and the size of an icon button: buttons
     * side by side line up.
     */
    val buttonHeight = 40.dp

    /** Corner radius of every button, so buttons side by side match. */
    val buttonCornerRadius = 12.dp
    val buttonShape = RoundedCornerShape(buttonCornerRadius)

    val cardShape = RoundedCornerShape(12.dp)
    val innerCardShape = RoundedCornerShape(8.dp)
}