package com.royaal.designsystem.util.textsource

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.royaal.designsystem.theme.AppTheme

/**
 *  Wrapper around any [CharSequence].
 *
 *  [value] - a [CharSequence] value that represents this text source.
 *  Usually this is a [String] or an [AnnotatedString].
 *
 *  [getLightedText] - same as [value], but with [lighters] applied
 *
 *  @see [CharSequence]
 */
abstract class TextSource(
    lighters: Set<Lighter> = emptySet()
) : CharSequence {
    @get:Composable
    abstract val value: AnnotatedString

    protected val lighters = lighters.toMutableSet()

    fun addLighter(l: Lighter) = lighters.add(l)

    @Composable
    fun getLightedText(): AnnotatedString {
        val baseAnnotatedString = value
        if (lighters.isEmpty()) return baseAnnotatedString

        return remember(baseAnnotatedString, lighters.toList()) {
            val text = baseAnnotatedString.text
            text.replace(Regex("\\p{C}"), "")

            AnnotatedString.Builder(baseAnnotatedString).apply {
                for (lighter in lighters) {
                    val matches = lighter.getMatches(text)
                    for (matchResult in matches) {
                        val start = matchResult.range.first
                        val end = matchResult.range.last + 1

                        if (start in 0..text.length && end <= text.length) {
                            val textChunk = text.subSequence(start, end)

                            addLink(
                                clickable = LinkAnnotation.Clickable(
                                    tag = "email_click_tag",
                                    styles = TextLinkStyles(
                                        style = lighter.themedLightUp()
                                    ),
                                    linkInteractionListener = {
                                        lighter.clickable(textChunk)
                                    }
                                ),
                                start = start,
                                end = end
                            )
                        }

                    }
                }
            }.toAnnotatedString()
        }
    }

    abstract val charValue: CharSequence

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return charValue.subSequence(startIndex, endIndex)
    }

    override fun get(index: Int): Char {
        return charValue[index]
    }

    override val length: Int
        get() = charValue.length

    /**
     *  Is used to highlight some part of text
     *  and make them clickable. Use [Regex] to
     *  produce [Sequence] of [MatchResult]'s.
     *
     *  If you want text to be clickable override
     *  [clickable] method. It should receive
     *  highlighted text.
     */
    interface Lighter {
        fun getMatches(text: CharSequence): Sequence<MatchResult>
        fun themedLightUp(): SpanStyle
        fun clickable(text: CharSequence) = Unit
    }
}

class HtmlTextSource(
    htmlText: String,
    lighters: Set<Lighter> = emptySet(),
) : TextSource(lighters) {

    override val value: AnnotatedString
        @Composable
        get() = AnnotatedString.fromHtml(
            htmlString = charValue,
            linkStyles = TextLinkStyles(
                style = SpanStyle(
                    color = AppTheme.linkColors.linkColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
        )

    override val charValue: String = htmlText
        .replace('\u2028','\n')
        .replace("\n", "<br>")
}

class RegularTextSource(
    text: CharSequence,
    lighters: Set<Lighter> = emptySet(),
) : TextSource(lighters) {
    override val value: AnnotatedString
        @Composable get() = charValue as? AnnotatedString
            ?: AnnotatedString(charValue.toString())

    override val charValue: CharSequence = text
}

class ResourceTextSource(
    private val context: () -> Context,
    @get:StringRes private val resId: Int,
    lighters: Set<Lighter> = emptySet(),
) : TextSource(lighters) {

    override val charValue: String
        get() = context().getString(resId)

    override val value: AnnotatedString
        @Composable
        get() = AnnotatedString(charValue)
}

class LinkTextSource(
    text: String,
    private val link: String,
    lighters: Set<Lighter> = emptySet(),
) : TextSource(lighters) {

    override val charValue: String = text

    override val value: AnnotatedString
        @Composable get() = buildAnnotatedString {
            withLink(
                LinkAnnotation.Url(
                    url = link,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = AppTheme.linkColors.linkColor,
                        )
                    )
                )
            ) {
                append(charValue)
            }
        }
}

class MultipartTextSource(
    val parts: List<TextSource>,
    override val charValue: CharSequence = parts.joinToString(separator = " ") { it.charValue },
    lighters: Set<Lighter> = emptySet(),
) : TextSource(lighters) {

    override val value: AnnotatedString
        @Composable get() = AnnotatedString.Builder().apply {
            for (i in parts.indices) {
                append(parts[i].value)
                if (i < parts.lastIndex) {
                    append(" ")
                }
            }
        }.toAnnotatedString()
}

fun String.asTextSource() = RegularTextSource(this)

fun AnnotatedString.asTextSource() = RegularTextSource(this)

@Composable
fun rememberAsHtml(text: String, vararg lighters: TextSource.Lighter) =
    remember(text, lighters) {
        derivedStateOf { HtmlTextSource(text, lighters.toSet()) }
    }

@Composable
fun rememberLighter(lighter: TextSource.Lighter) = remember { lighter }