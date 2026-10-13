package com.royaal.designsystem.theme

import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.royaal.designsystem.generated.resources.Res
import com.royaal.designsystem.generated.resources.source_code_pro
import com.royaal.designsystem.generated.resources.source_serif_4
import com.royaal.designsystem.util.BasePreview
import org.jetbrains.compose.resources.Font

private val baseline = Typography()

// org.jetbrains.compose.resources.Font() is @Composable (resolves the platform resource at
// composition time), so the font-dependent typography can't be a plain top-level val anymore.
@Suppress("ComposableNaming")
@Composable
fun AppTypography(): Typography {
    val sourceCodePro = FontFamily(Font(Res.font.source_code_pro))
    val sourceSerif4 = FontFamily(Font(Res.font.source_serif_4))

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = sourceSerif4),
        displayMedium = baseline.displayMedium.copy(fontFamily = sourceSerif4),
        displaySmall = baseline.displaySmall.copy(fontFamily = sourceSerif4),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = sourceSerif4),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = sourceSerif4),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = sourceSerif4),
        titleLarge = baseline.titleLarge.copy(fontFamily = sourceCodePro),
        titleMedium = baseline.titleMedium.copy(fontFamily = sourceCodePro),
        titleSmall = baseline.titleSmall.copy(fontFamily = sourceCodePro),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = sourceCodePro),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = sourceCodePro),
        bodySmall = baseline.bodySmall.copy(fontFamily = sourceCodePro),
        labelLarge = baseline.labelLarge.copy(fontFamily = sourceCodePro),
        labelMedium = baseline.labelMedium.copy(fontFamily = sourceCodePro),
        labelSmall = baseline.labelSmall.copy(fontFamily = sourceCodePro),
    )
}

@Preview
@Composable
private fun Preview() = BasePreview {
    val typography = AppTypography()
    Text("Display large", style = typography.displayLarge)
    Text("Display medium", style = typography.displayMedium)
    Text("Display small", style = typography.displaySmall)
    Text("Headline Large", style = typography.headlineLarge)
    Text("Headline Medium", style = typography.headlineMedium)
    Text("Headline Small", style = typography.headlineSmall)
    Text("Title large", style = typography.titleLarge)
    Text("Title medium", style = typography.titleMedium)
    Text("Text example", style = typography.titleSmall)
    Text("Text example", style = typography.bodyLarge)
    Text("Text example", style = typography.bodyMedium)
    Text("Text example", style = typography.bodySmall)
    Text("Text example", style = typography.labelLarge)
    Text("Text example", style = typography.labelMedium)
    Text("Text example", style = typography.labelSmall)
}