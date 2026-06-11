package com.example.calculator.layout.functions

import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.example.calculator.layout.keyboard.backspaceDeleteRange
import com.example.calculator.layout.keyboard.moveCursorLeftStructurally
import com.example.calculator.layout.keyboard.moveCursorRightStructurally
import com.example.calculator.parser.isResultFinalized

fun functions_pressed_key(current_key_pressed: String) {
    if (active_function_index !in functions_list.indices) return
    val state = functions_list[active_function_index]

    when (current_key_pressed) {
        "\\(2^{nd})" -> functions_keyboard_in_use = 2
        "\\(1^{st})" -> functions_keyboard_in_use = 1
        "C" -> {
            functions_list.forEach { it.edit { replace(0, length, "") } }
            active_function_index = 0
            isResultFinalized.value = false
        }
        "◀" -> state.edit {
            val pos = selection.start.coerceIn(0, length)
            val text = toString()
            selection = TextRange(moveCursorLeftStructurally(text, pos))
        }
        "▶" -> state.edit {
            val pos = selection.start.coerceIn(0, length)
            val text = toString()
            selection = TextRange(moveCursorRightStructurally(text, pos))
        }
        "▲" -> {
            if (active_function_index > 0) active_function_index--
        }
        "▼" -> {
            if (active_function_index < functions_list.size - 1) active_function_index++
        }
        "⌫" -> {
            val pos = state.selection.start.coerceIn(0, state.text.length)
            if (pos == 0 && state.text.isEmpty() && functions_list.size > 1) {
                functions_list.removeAt(active_function_index)
                active_function_index = (active_function_index - 1).coerceAtLeast(0)
            } else if (pos > 0) {
                state.edit {
                    val text = toString()
                    val (from, to) = backspaceDeleteRange(text, pos)
                    delete(from, to)
                    selection = TextRange(from.coerceIn(0, length))
                }
            }
            isResultFinalized.value = false
        }
        "+/-" -> state.edit {
            val text = toString()
            val cur = selection.start.coerceIn(0, length)

            var start = cur
            while (start > 0 && (text[start - 1].isDigit() || text[start - 1] == '.')) {
                start--
            }

            if (start > 0 && (text[start - 1] == '+' || text[start - 1] == '-')) {
                val currentSign = text[start - 1]
                val newSign = if (currentSign == '+') "-" else "+"
                replace(start - 1, start, newSign)
            } else {
                insert(start, "-")
            }
            isResultFinalized.value = false
        }
        "f(x))" -> state.edit {
            val text = toString()
            if (!text.contains("f_{")) {
                val n = active_function_index + 1
                val insertion = "f_{$n}(x) ="
                insert(0, insertion)
                selection = TextRange(insertion.length)
                isResultFinalized.value = false
            }
        }
        else -> state.edit {
            var pos = selection.start.coerceIn(0, length)

            if (current_key_pressed == "\\(x^{2})" || current_key_pressed == "\\(x^{3})" || current_key_pressed == "\\(x^{y})") {
                val exponent = when (current_key_pressed) {
                    "\\(x^{2})" -> "2"
                    "\\(x^{3})" -> "3"
                    else -> ""
                }

                var baseStart = pos
                val textStr = toString()
                
                if (baseStart > 0) {
                    val lastChar = textStr[baseStart - 1]
                    if (lastChar.isLetter()) {
                        baseStart--
                    } else if (lastChar.isDigit()) {
                        while (baseStart > 0 && textStr[baseStart - 1].isDigit()) {
                            baseStart--
                        }
                    } else if (lastChar == ')') {
                        var depth = 1
                        baseStart--
                        while (baseStart > 0 && depth > 0) {
                            if (textStr[baseStart - 1] == ')') depth++
                            else if (textStr[baseStart - 1] == '(') depth--
                            baseStart--
                        }
                    }
                }
                val attachToExistingBase = baseStart < pos

                if (attachToExistingBase) {
                    val baseStr = textStr.substring(baseStart, pos)
                    replace(baseStart, pos, "($baseStr)^{($exponent)}")
                    val delta = if (exponent.isEmpty()) baseStr.length + 5 else baseStr.length + 5 + exponent.length
                    selection = TextRange((baseStart + delta).coerceIn(0, length))
                } else {
                    val insertion = "()^{($exponent)}"
                    insert(pos, insertion)
                    selection = TextRange((pos + 1).coerceIn(0, length))
                }
                isResultFinalized.value = false
                return@edit
            }

            val insertion = keyToLatex(current_key_pressed)
            if (insertion.isEmpty()) return@edit

            insert(pos, insertion)

            val delta = insertionCursorDelta(insertion)
            selection = TextRange((pos + delta).coerceIn(0, length))
            isResultFinalized.value = false
        }
    }
    if (functions_keyboard_in_use == 2 && (current_key_pressed != "\\(2^{nd})" && current_key_pressed != "\\(1^{st})")) {
        functions_keyboard_in_use = 1
    }
}

private fun keyToLatex(key: String): String = when (key) {
    "\\(2^{nd})", "\\(1^{st})" -> ""
    "\\pi", "π" -> "\\pi"
    "\\div", "÷" -> "\\frac{()}{()}"
    "\\times", "×" -> "\\times"
    "\\sin" -> "\\sin()"
    "\\cos" -> "\\cos()"
    "\\tan" -> "\\tan()"
    "\\sinh" -> "\\sinh()"
    "\\cosh" -> "\\cosh()"
    "\\tanh" -> "\\tanh()"
    "\\log" -> "\\log()"
    "\\ln" -> "\\ln()"
    "\\exp" -> "\\exp()"
    "\\(sin^{-1})" -> "\\sin^{-1}()"
    "\\(cos^{-1})" -> "\\cos^{-1}()"
    "\\(tan^{-1})" -> "\\tan^{-1}()"
    "\\(sinh^{-1})" -> "\\sinh^{-1}()"
    "\\(cosh^{-1})" -> "\\cosh^{-1}()"
    "\\(tanh^{-1})" -> "\\tanh^{-1}()"
    "\\(x^{2})" -> "()^{(2)}"
    "\\(x^{3})" -> "()^{(3)}"
    "\\(x^{y})" -> "()^{()}"
    "\\(10^{x})" -> "10^{()}"
    "\\(2^{x})" -> "2^{()}"
    "\\(e^{x})" -> "e^{()}"
    "\\sqrt{x}", "√x" -> "\\sqrt{()}"
    "\\sqrt[3]{x}" -> "\\sqrt[3]{()}"
    "\\sqrt[y]{x}" -> "\\sqrt[()]{()}"
    "\\(log_{y}x)" -> "\\log_{()}()"
    "\\frac{1}{x}" -> "\\frac{(1)}{()}"
    "\\bmod" -> "\\bmod"
    "\\left|x\\right|" -> "|()|"
    "x!" -> "!"
    "x" -> "x"
    "y" -> "y"
    "z" -> "z"
    "0" -> "0"
    "1" -> "1"
    "2" -> "2"
    "3" -> "3"
    "4" -> "4"
    "5" -> "5"
    "6" -> "6"
    "7" -> "7"
    "8" -> "8"
    "9" -> "9"
    "." -> "."
    "<" -> "<"
    ">" -> ">"
    "≤" -> "\\le"
    "≥" -> "\\ge"
    "=" -> "="
    "C", "⌫", "◀", "▶", "▲", "▼", "ans", "+/-" -> ""

    else -> key
}

private fun insertionCursorDelta(inserted: String): Int {
    inserted.indexOf("{}").takeIf { it >= 0 }?.let { return it + 1 }
    inserted.indexOf("()").takeIf { it >= 0 }?.let { return it + 1 }
    inserted.indexOf("[]").takeIf { it >= 0 }?.let { return it + 1 }
    return inserted.length
}
