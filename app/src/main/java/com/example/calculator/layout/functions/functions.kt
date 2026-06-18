package com.example.calculator.layout.functions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.calculator.layout.common.LatexEntryField
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.example.calculator.layout.standard.toLatex
import androidx.compose.runtime.LaunchedEffect
import com.example.calculator.parser.live_result
import com.example.calculator.parser.isResultFinalized
import kotlinx.coroutines.yield
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background

val FunctionsKeyboard_st = listOf(
    "\\(2^{nd})", "\\pi", "e", "▲", "⌫",
    "\\(x^{2})", "\\frac{1}{x}", "◀", "x", "▶",
    "\\sqrt{x}", "(", ")", "▼", "\\div",
    "\\(x^{y})", "7", "8", "9", "\\times",
    "\\(10^{x})", "4", "5", "6", "-",
    "\\log", "1", "2", "3", "+",
    "f(x))", "+/-", "0", ".", "="
)

val FunctionsKeyboard_nd = listOf(
    "\\(1^{st})", "\\pi", "e", "C", "⌫",
    "\\(x^{3})", "\\frac{1}{x}", "\\left|x\\right|", "x", "\\bmod",
    "\\sqrt[3]{x}", "(", ")", "x!", "\\div",
    "\\sqrt[y]{x}", "\\sin", "\\cos", "\\tan", "\\times",
    "\\(2^{x})", "\\(sin^{-1})", "\\(cos^{-1})", "\\(tan^{-1})", "-",
    "\\(log_{y}x)", "\\sinh", "\\cosh", "\\tanh", "+",
    "\\(e^{x})", "\\(sinh^{-1})", "\\(cosh^{-1})", "\\(tanh^{-1})", "ans"
)

var functions_keyboard_in_use by mutableStateOf(1)
val functions_list = mutableStateListOf(TextFieldState(""))
var active_function_index by mutableStateOf(0)
var showGraphView by mutableStateOf(false)

@Composable
fun InitFunctionsCalculator(
    paddingValues: PaddingValues,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    LaunchedEffect(functions_list.toList()) {
        delay(150)
        val combined = functions_list.joinToString(" ; ") { it.text.toString() }
        withContext(Dispatchers.Default) {
            com.example.calculator.parser.evaluateExpression(combined, isLive = true)
        }
    }

    functions_list.forEachIndexed { _, state ->
        LaunchedEffect(state.text) {
            delay(150)
            val combined = functions_list.joinToString(" ; ") { it.text.toString() }
            withContext(Dispatchers.Default) {
                com.example.calculator.parser.evaluateExpression(combined, isLive = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showGraphView = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Text("View", fontSize = 18.sp, color = Color.White)
            }

            Button(
                onClick = {
                    functions_list.add(TextFieldState(""))
                    active_function_index = functions_list.size - 1
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            val mainScrollState = rememberScrollState()

            LaunchedEffect(live_result.value, functions_list.size) {
                repeat(2) {
                    yield()
                    mainScrollState.scrollTo(mainScrollState.maxValue)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(mainScrollState),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    functions_list.forEachIndexed { index, state ->
                        val isFocused = active_function_index == index

                        val scrollState = rememberScrollState()
                        LaunchedEffect(state.text, state.selection) {
                            if (isFocused) {
                                repeat(2) { yield() }
                                val textLength = state.text.length
                                if (textLength > 0) {
                                    val cursorPosition = state.selection.start
                                    if (cursorPosition == textLength) {
                                        scrollState.scrollTo(scrollState.maxValue)
                                    } else {
                                        val ratio = cursorPosition.toFloat() / textLength
                                        scrollState.scrollTo((scrollState.maxValue * ratio).toInt())
                                    }
                                } else {
                                    scrollState.scrollTo(0)
                                }
                            }
                        }

                        val fontSizeExpr = if (isResultFinalized.value) 30.sp else 48.sp
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFocused) Color.DarkGray else Color.DarkGray.copy(alpha = 0.5f))
                                .clickable { active_function_index = index }
                        ) {
                            LatexEntryField(
                                state = state,
                                isFocused = isFocused,
                                modifier = Modifier
                                    .horizontalScroll(scrollState)
                                    .padding(16.dp),
                                config = LatexConfig(
                                    fontSize = fontSizeExpr,
                                    color = if (isResultFinalized.value) Color.Gray else Color.White,
                                    darkColor = if (isResultFinalized.value) Color.Gray else Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        MakeFunctionsKeyboard(
            isDarkMode = isDarkMode,
            onDarkModeChange = onDarkModeChange
        )
    }

    if (showGraphView) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showGraphView = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GraphCanvas(modifier = Modifier.fillMaxSize())

                    Button(
                        onClick = { showGraphView = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Text("X", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MakeFunctionsKeyboard(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    val latexConfig = remember {
        LatexConfig(
            fontSize = 20.sp,
            color = Color.White,
            darkColor = Color.White
        )
    }

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
                                record_function_key_press(
                                    index = i,
                                    keyboard = functions_keyboard_in_use
                                )
                            },
                            modifier = Modifier
                                .padding(all = 0.dp)
                                .weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            val raw = if (functions_keyboard_in_use == 1) {
                                FunctionsKeyboard_st[i]
                            } else {
                                FunctionsKeyboard_nd[i]
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
                                val raw = if (functions_keyboard_in_use == 1) {
                                    FunctionsKeyboard_st[i]
                                } else {
                                    FunctionsKeyboard_nd[i]
                                }
                                val latexText = raw
                                    .removePrefix("\\(")
                                    .removeSuffix(")")
                                if (latexText == "\\frac{1}{x}"){
                                    Latex(
                                        latex = "1/x",
                                        config = latexConfig
                                    )
                                }
                                else {
                                    Latex(
                                        latex = latexText,
                                        config = latexConfig
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

fun record_function_key_press(index: Int, keyboard: Int) {
    if (keyboard == 1) {
        functions_pressed_key(FunctionsKeyboard_st[index])
    }
    else {
        functions_pressed_key(FunctionsKeyboard_nd[index])
    }
}
