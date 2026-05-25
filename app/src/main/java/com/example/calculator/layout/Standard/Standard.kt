package com.example.calculator.layout.Standard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

val StandardKeyboard_st = listOf(
    "\\(2^{nd}\\)", "π", "e", "C", "⌫",
    "\\(x^{2}\\)", "1/x", "|x|", "exp", "%",
    "\\(^{2}\\)√x", "(", ")", "n!", "÷",
    "\\(x^{y}\\)", "7", "8", "9", "X",
    "\\(10^{x}\\)", "4", "5", "6", "-",
    "log", "1", "2", "3", "+",
    "ln", "+/-", "0", ".", "=")

val StandardKeyboard_nd = listOf(
    "\\(1^{st}\\)", "π", "e", "C", "⌫",
    "\\(x^{3}\\)", "1/x", "|x|", "exp", "%",
    "\\(^{3}\\)√x", "(", ")", "n!", "÷",
    "\\(^{y}\\)√x", "7", "8", "9", "X",
    "\\(2^{x}\\)", "4", "5", "6", "-",
    "log\\(ˇ{y}x\\)", "1", "2", "3", "+",
    "\\(e^{x}\\)", "+/-", "0", ".", "=")

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
        makeStandardKeyboard(
            paddingValues = paddingValues,
            isDarkMode = isDarkMode,
            onDarkModeChange = onDarkModeChange)
    }
}

fun makeStandardKeyboard(
    paddingValues: PaddingValues,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        for(i in 0..6){
            for(j in 0.. 4){
                
            }
        }
    }

}
