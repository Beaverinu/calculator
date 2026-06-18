package com.example.calculator.layout.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.calculator.layout.standard.toLatex
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig

@Composable
fun LatexEntryField(
    state: TextFieldState,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    config: LatexConfig = LatexConfig()
) {
    val latexText = toLatex(
        input = state.text.toString(),
        cursorIndex = if (isFocused) state.selection.start else -1
    )

    Latex(
        latex = latexText,
        modifier = modifier.fillMaxWidth(),
        config = config
    )
}
