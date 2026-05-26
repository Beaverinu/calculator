package com.example.calculator.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.MaterialTheme

@Composable
fun AutoResizingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    var resizedTextStyle by remember(text, style) { mutableStateOf(style) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        style = resizedTextStyle,
        softWrap = false,
        onTextLayout = { result ->
            if (result.didOverflowWidth) {
                resizedTextStyle = resizedTextStyle.copy(
                    fontSize = resizedTextStyle.fontSize * 0.95
                )
            }
            else {
                readyToDraw = true
            }
        },
    )
}
@Composable
fun FakeCursorText(
    text: String,
    cursorIndex: Int,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color = Color.Unspecified,
    cursorColor: Color = Color.White,
    cursorWidthPx: Float = 6f,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var resizedTextStyle by remember(text, color, style) {
        mutableStateOf(style.copy(textAlign = TextAlign.End, color = color)) 
    }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    val clampedIndex = cursorIndex.coerceIn(0, text.length)

    Box(
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()

                val lr = layoutResult ?: return@drawWithContent
                val rect = lr.getCursorRect(clampedIndex)

                val isSuperscript = (clampedIndex > 0 && text[clampedIndex - 1].isSuperscript()) ||
                                    (clampedIndex < text.length && text[clampedIndex].isSuperscript())
                val isSubscript = (clampedIndex > 0 && text[clampedIndex - 1].isSubscript()) ||
                                  (clampedIndex < text.length && text[clampedIndex].isSubscript())

                var top = rect.top
                var bottom = rect.bottom
                
                if (isSuperscript) {
                    bottom = top + (rect.height * 0.5f)
                } else if (isSubscript) {
                    top = bottom - (rect.height * 0.5f)
                }

                drawLine(
                    color = cursorColor,
                    start = Offset(rect.left, top),
                    end = Offset(rect.left, bottom),
                    strokeWidth = cursorWidthPx
                )
            }
        }
    ) {
        BasicText(
            text = text,
            style = resizedTextStyle,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = resizedTextStyle.fontSize * 0.95
                    )
                } else {
                    layoutResult = result
                    readyToDraw = true
                }
            },
        )
    }
}

private fun Char.isSuperscript(): Boolean = this in "⁰¹²³⁴⁵⁶⁷⁸⁹ˣʸ⁺⁻⁽⁾"
private fun Char.isSubscript(): Boolean = this in "₀₁₂₃₄₅₆₇₈₉ₓᵧ₊₋₍₎"
