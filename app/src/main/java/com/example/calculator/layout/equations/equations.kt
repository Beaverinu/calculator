package com.example.calculator.layout.equations

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.example.calculator.layout.standard.toLatex

val EquationsKeyboard_st = listOf(
    "\\(2^{nd})", "<", ">", "▲", "⌫",
    "x", "y", "◀", "z", "▶",
    "\\sqrt{x}", "(", ")", "▼", "\\div",
    "\\(x^{y})", "7", "8", "9", "\\times",
    "\\(10^{x})", "4", "5", "6", "-",
    "\\log", "1", "2", "3", "+",
    "=", "+/-", "0", ".", "ans"
)

val EquationsKeyboard_nd = listOf(
    "\\(1^{st})", "≤", "≥", "C", "⌫",
    "\\(x^{2})", "\\(x^{3})", "\\left|x\\right|", "\\exp", "\\bmod",
    "\\sqrt[3]{x}", "(", ")", "x!", "\\div",
    "\\sqrt[y]{x}", "\\sin", "\\cos", "\\tan", "\\times",
    "\\(2^{x})", "\\(sin^{-1})", "\\(cos^{-1})", "\\(tan^{-1})", "-",
    "\\(log_{y}x)", "\\sinh", "\\cosh", "\\tanh", "+",
    "\\(e^{x})", "\\(sinh^{-1})", "\\(cosh^{-1})", "\\(tanh^{-1})", "ans"
)

var equations_keyboard_in_use by mutableStateOf(1)
val equations_text_state = TextFieldState("")

@Composable
fun InitEquationsCalculator(
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
            val latexText = toLatex(
                input = equations_text_state.text.toString(),
                cursorIndex = equations_text_state.selection.start
            )
            Column() {
                Text(
                    text = latexText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    lineHeight = 48.sp
                )

                Latex(
                    latex = latexText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    config = LatexConfig(
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        MakeEquationsKeyboard(
            isDarkMode = isDarkMode,
            onDarkModeChange = onDarkModeChange
        )
    }
}

@Composable
fun MakeEquationsKeyboard(
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
                                record_equation_key_press(
                                    index = i,
                                    keyboard = equations_keyboard_in_use
                                )
                            },
                            modifier = Modifier
                                .padding(all = 0.dp)
                                .weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            val raw = if (equations_keyboard_in_use == 1) {
                                EquationsKeyboard_st[i]
                            } else {
                                EquationsKeyboard_nd[i]
                            }

                            val uiKeys = setOf("▲", "▼", "◀", "▶", "⌫", "C", "ans", "+/-", "=", "(", ")", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "<", ">", "≤", "≥")

                            if (raw in uiKeys) {
                                Text(
                                    text = raw,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                            else {
                                val latexText = raw
                                    .removePrefix("\\(")
                                    .removeSuffix(")")
                                
                                Latex(
                                    latex = latexText,
                                    config = LatexConfig(
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun record_equation_key_press(index: Int, keyboard: Int) {
    if (keyboard == 1) {
        equations_pressed_key(EquationsKeyboard_st[index])
    }
    else {
        equations_pressed_key(EquationsKeyboard_nd[index])
    }
}
