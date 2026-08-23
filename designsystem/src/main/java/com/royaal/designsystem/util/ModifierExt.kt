package com.royaal.designsystem.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

fun Modifier.conditional(
    condition: () -> Boolean,
    then: Modifier.() -> Modifier,
) = if (condition()) then() else this

fun Modifier.conditional(
    condition: Boolean,
    then: Modifier.() -> Modifier
) = if (condition) then else this

@Composable
fun Modifier.clickable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    role: Role? = null,
) = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick,
    enabled = enabled,
    role = role,
)