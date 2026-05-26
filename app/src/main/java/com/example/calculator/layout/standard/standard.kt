package com.example.calculator.layout.standard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextLayoutResult

val StandardKeyboard_st = listOf(
    "2ⁿᵈ", "π", "e", "▲", "⌫",
    "x²", "1/x", "◀", "exp", "▶",
    "√x", "(", ")", "▼", "÷",
    "xʸ", "7", "8", "9", "×",
    "10ˣ", "4", "5", "6", "-",
    "log", "1", "2", "3", "+",
    "ln", "+/-", "0", ".", "="
)

val StandardKeyboard_nd = listOf(
    "1ˢᵗ", "π", "e", "C", "⌫",
    "x³", "1/x", "|x|", "exp", "%",
    "³√x", "(", ")", "x!", "÷",
    "ʸ√x", "sin", "cos", "tan", "×",
    "2ˣ", "sin⁻¹", "cos⁻¹", "tan⁻¹", "-",
    "logᵧx", "sinh", "cosh", "tanh", "+",
    "eˣ", "sinh⁻¹", "cosh⁻¹", "tanh⁻¹", "ans"
)
var current_keyboard_in_use by mutableStateOf(1)
val current_text_state = TextFieldState("0")

@Composable
fun InitStandardCalculator(
    paddingValues: PaddingValues,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd

        ) {
            FakeCursorText(
                text = current_text_state.text.toString(),
                cursorIndex = current_text_state.selection.start,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        }

        MakeStandardKeyboard(
            isDarkMode = isDarkMode,
            onDarkModeChange = onDarkModeChange
        )
    }
}

@Composable
fun MakeStandardKeyboard(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val rows = (0..34).chunked(5)
            for (rowIndices in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in rowIndices) {
                        Button(
                            onClick = {
                                record_key_press(
                                    index = i,
                                    keyboard = current_keyboard_in_use
                                )
                            },
                            modifier = Modifier
                                .padding(all = 0.dp)
                                .weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            val text = if (current_keyboard_in_use == 1) {
                                StandardKeyboard_st[i]
                            }
                            else if (current_keyboard_in_use == 2){
                                StandardKeyboard_nd[i]
                            }
                            else {
                                StandardKeyboard_st[i]
                            }
                            AutoResizingText(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                                modifier = Modifier.padding(all = 0.dp)
                            )
                        }
                    }
                }
            }
        }
    }

}

fun record_key_press(index: Int, keyboard: Int) {
    if (keyboard == 1) {
        pressed_key(StandardKeyboard_st[index])
    }
    else if (keyboard == 2) {
        pressed_key(StandardKeyboard_nd[index])
    }
    else {
        pressed_key(StandardKeyboard_st[index])
    }
}

@Composable
fun AutoResizingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    var resizedTextStyle by remember(text) { mutableStateOf(style) }
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
    cursorColor: Color = Color.White,
    cursorWidthPx: Float = 6f,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var resizedTextStyle by remember(text) { mutableStateOf(style.copy(textAlign = TextAlign.End)) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    val clampedIndex = cursorIndex.coerceIn(0, text.length)

    Box(
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()

                val lr = layoutResult ?: return@drawWithContent
                val rect = lr.getCursorRect(clampedIndex)

                drawLine(
                    color = cursorColor,
                    start = Offset(rect.left, rect.top),
                    end = Offset(rect.left, rect.bottom),
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
