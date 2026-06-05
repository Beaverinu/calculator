package com.example.calculator.layout.equations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.runtime.LaunchedEffect
import com.example.calculator.parser.live_result
import com.example.calculator.parser.isResultFinalized
import kotlinx.coroutines.yield

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background

val EquationsKeyboard_st = listOf(
    "\\(2^{nd})", "</≤", ">/≥", "▲", "⌫",
    "\\(x^{2})", "\\frac{1}{x}", "◀", "x/y/z", "▶",
    "\\sqrt{x}", "(", ")", "▼", "\\div",
    "\\(x^{y})", "7", "8", "9", "\\times",
    "\\(10^{x})", "4", "5", "6", "-",
    "\\log", "1", "2", "3", "+",
    "=", "+/-", "0", ".", "ans"
)


val EquationsKeyboard_nd = listOf(
    "\\(1^{st})", "\\pi", "e", "C", "⌫",
    "\\(x^{3})", "\\frac{1}{x}", "\\left|x\\right|", "x/y/z", "\\bmod",
    "\\sqrt[3]{x}", "(", ")", "x!", "\\div",
    "\\sqrt[y]{x}", "\\sin", "\\cos", "\\tan", "\\times",
    "\\(2^{x})", "\\(sin^{-1})", "\\(cos^{-1})", "\\(tan^{-1})", "-",
    "\\(log_{y}x)", "\\sinh", "\\cosh", "\\tanh", "+",
    "\\(e^{x})", "\\(sinh^{-1})", "\\(cosh^{-1})", "\\(tanh^{-1})", "ans"
)

var equations_keyboard_in_use by mutableStateOf(1)
val equations_list = mutableStateListOf(TextFieldState(""))
var active_equation_index by mutableStateOf(0)

@Composable
fun InitEquationsCalculator(
    paddingValues: PaddingValues,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    LaunchedEffect(equations_list.size) {
        // Trigger live result update when windows are added/removed
        val combined = equations_list.joinToString(" ; ") { it.text.toString() }
        com.example.calculator.parser.evaluateExpression(combined, isLive = true)
    }

    // Individual text observers for each window
    equations_list.forEachIndexed { index, state ->
        LaunchedEffect(state.text) {
            val combined = equations_list.joinToString(" ; ") { it.text.toString() }
            com.example.calculator.parser.evaluateExpression(combined, isLive = true)
        }
    }

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
            val equationScrollState = rememberScrollState()
            val resultsScrollState = rememberScrollState()
            val resultsVerticalScrollState = rememberScrollState()

            LaunchedEffect(live_result.value) {
                repeat(2) {
                    yield()
                    resultsScrollState.scrollTo(resultsScrollState.maxValue)
                    resultsVerticalScrollState.scrollTo(resultsVerticalScrollState.maxValue)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.End
            ) {
                // Add Equation Button at the top
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            equations_list.add(TextFieldState(""))
                            active_equation_index = equations_list.size - 1
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text("+", fontSize = 24.sp, color = Color.White)
                    }
                }

                // Expression Windows
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(equationScrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    equations_list.forEachIndexed { index, state ->
                        val isFocused = active_equation_index == index
                        val latexText = toLatex(
                            input = state.text.toString(),
                            cursorIndex = if (isFocused) state.selection.start else -1
                        )

                        val fontSizeExpr = if (isResultFinalized.value) 30.sp else 48.sp
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFocused) Color.DarkGray else Color.DarkGray.copy(alpha = 0.5f))
                                .clickable { active_equation_index = index }
                                .padding(16.dp)
                        ) {
                            Latex(
                                latex = latexText,
                                config = LatexConfig(
                                    fontSize = fontSizeExpr,
                                    color = if (isResultFinalized.value) Color.Gray else Color.White,
                                    darkColor = if (isResultFinalized.value) Color.Gray else Color.White
                                )
                            )
                        }
                    }
                }

                // Results Area
                val resultLines = live_result.value
                    .split(" ; ")
                    .filter { it.isNotBlank() }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(resultsVerticalScrollState),
                        horizontalAlignment = Alignment.End
                    ) {
                        resultLines.forEach { line ->
                            Latex(
                                latex = line,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(resultsScrollState)
                                    .padding(8.dp),
                                config = LatexConfig(
                                    fontSize = if (isResultFinalized.value) 48.sp else 30.sp,
                                    color = if (isResultFinalized.value) Color.White else Color.Gray,
                                    darkColor = if (isResultFinalized.value) Color.White else Color.Gray
                                )
                            )
                        }
                    }
                }
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

                            val uiKeys = setOf("▲", "▼", "◀", "▶", "⌫", "C", "ans", "+/-", "=", "(", ")", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "<", ">", "≤", "≥", "x/y/z", "</≤", ">/≥")

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
                                if (latexText == "\\frac{1}{x}"){
                                    Latex(
                                        latex = "1/x",
                                        config = LatexConfig(
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            darkColor = Color.White
                                        )
                                    )
                                }
                                else {
                                    Latex(
                                        latex = latexText,
                                        config = LatexConfig(
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            darkColor = Color.White
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
}

fun record_equation_key_press(index: Int, keyboard: Int) {
    if (keyboard == 1) {
        equations_pressed_key(EquationsKeyboard_st[index])
    }
    else {
        equations_pressed_key(EquationsKeyboard_nd[index])
    }
}
