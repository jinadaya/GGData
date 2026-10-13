package com.royaal.designsystem.uikit

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.BasePreview

@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTheme.typography.displaySmall,
        color = AppTheme.textColors.primary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Preview
@Composable
private fun Preview() = BasePreview {
    Title("Title")
}