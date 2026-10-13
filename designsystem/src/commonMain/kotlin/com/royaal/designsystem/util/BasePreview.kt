package com.royaal.designsystem.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.theme.AppTheme

@Composable
fun BasePreview(
    isDarkTheme: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) = AppTheme(isDarkTheme) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}