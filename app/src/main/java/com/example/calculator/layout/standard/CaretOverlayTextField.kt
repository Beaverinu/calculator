package com.example.calculator.layout.standard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextToolbar
import androidx.compose.ui.text.input.TextToolbarStatus
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private object DisabledTextToolbar : TextToolbar {
    override var status: TextToolbarStatus = TextToolbarStatus.Hidden

    override fun hide() {
        status = TextToolbarStatus.Hidden
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        status = TextToolbarStatus.Hidden
    }
}

@Composable
fun CaretOverlayTextField(
    state: TextFieldState,
    cursorIndex: Int,
    onCursorIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle,
    caretColor: Color,
    textPadding: Dp = 16.dp
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val text = state.text.toString()
    val safeCursorIndex = cursorIndex.coerceIn(0, text.length)

    LaunchedEffect(text.length, cursorIndex) {
        val clamped = cursorIndex.coerceIn(0, text.length)
        if (clamped != cursorIndex) {
            onCursorIndexChange(clamped)
        }
    }

    CompositionLocalProvider(LocalTextToolbar provides DisabledTextToolbar) {
        Box(
            modifier = modifier.drawWithContent {
                drawContent()
                val layout = textLayoutResult ?: return@drawWithContent
                val caretRect = layout.getCursorRect(safeCursorIndex)
                val x = caretRect.left.coerceIn(0f, size.width)
                drawLine(
                    color = caretColor,
                    start = Offset(x = x, y = caretRect.top),
                    end = Offset(x = x, y = caretRect.bottom),
                    strokeWidth = 2.dp.toPx()
                )
            }
        ) {
            TextField(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                textStyle = textStyle,
                readOnly = true,
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Transparent
                )
            )

            BasicText(
                text = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = textPadding),
                style = textStyle.copy(color = Color.Transparent),
                maxLines = 1,
                softWrap = false,
                onTextLayout = { textLayoutResult = it }
            )
        }
    }
}
