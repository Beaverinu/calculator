package com.example.calculator.layout.standard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.foundation.text.input.TextFieldState

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
var cursorIndex by mutableIntStateOf(1)
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
            CaretOverlayTextField(
                state = current_text_state,
                cursorIndex = cursorIndex,
                onCursorIndexChange = { cursorIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    textAlign = TextAlign.End,
                    fontSize = 48.sp
                ),
                caretColor = MaterialTheme.colorScheme.primary
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
