package com.example.calculator.layout.standard

import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.example.calculator.parser.previous_ans
import com.example.calculator.layout.keyboard.backspaceDeleteRange
import com.example.calculator.layout.keyboard.moveCursorLeftStructurally
import com.example.calculator.layout.keyboard.moveCursorRightStructurally

fun pressed_key(current_key_pressed: String) {
    val state = current_text_state

    when (current_key_pressed) {
        "\\(2^{nd})" -> current_keyboard_in_use = 2
        "\\(1^{st})" -> current_keyboard_in_use = 1
        "C" -> state.edit {
            replace(0, length, "")
            selection = TextRange(0)
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
        "▲" -> state.edit {
            val pos = selection.start.coerceIn(0, length)
            val text = toString()
            var s = pos
            while (s < length) {
                if (text[s] == '^') {
                    val match = com.example.calculator.layout.keyboard.matchFunctionCursorAt(text, s)
                    if (match != null) {
                        selection = TextRange(match.argumentIndex)
                        return@edit
                    }
                }
                s++
            }
        }
        "▼" -> state.edit {
            val pos = selection.start.coerceIn(0, length)
            val text = toString()
            var s = pos
            while (s < length) {
                if (text[s] == '_') {
                    val match = com.example.calculator.layout.keyboard.matchFunctionCursorAt(text, s)
                    if (match != null) {
                        selection = TextRange(match.argumentIndex)
                        return@edit
                    }
                }
                s++
            }
            selection = TextRange(length)
        }
        "⌫" -> state.edit {
            val pos = selection.start.coerceIn(0, length)
            if (pos > 0) {
                val text = toString()
                val (from, to) = backspaceDeleteRange(text, pos)
                delete(from, to)
                selection = TextRange(from.coerceIn(0, length))
            }
        }
        "ans" -> state.edit {
            val pos = selection.start.coerceIn(0, length)
            val v = previous_ans.value
            insert(pos, v)
            selection = TextRange((pos + v.length).coerceIn(0, length))
        }
        "=" -> Unit
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
                
                while (baseStart > 0 && textStr[baseStart - 1].isDigit()) {
                    baseStart--
                }
                val attachToExistingBase = baseStart < pos

                if (attachToExistingBase) {
                    val baseStr = textStr.substring(baseStart, pos)
                    replace(baseStart, pos, "($baseStr)^{($exponent)}")
                    val delta = baseStr.length + 5 // length of (base)^{(exp)} - 1 to be inside exp
                    selection = TextRange((baseStart + delta).coerceIn(0, length))
                } else {
                    val insertion = "()^{($exponent)}"
                    insert(pos, insertion)
                    val delta = insertionCursorDelta(insertion)
                    selection = TextRange((pos + delta).coerceIn(0, length))
                }
                return@edit
            }

            val insertion = keyToLatex(current_key_pressed)
            if (insertion.isEmpty()) return@edit

            insert(pos, insertion)

            val delta = insertionCursorDelta(insertion)
            selection = TextRange((pos + delta).coerceIn(0, length))
        }
    }
    if (current_keyboard_in_use == 2 && (current_key_pressed != "\\(2^{nd})" && current_key_pressed != "\\(1^{st})")) {
        current_keyboard_in_use = 1
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
    "\\(log_{y}x)" -> "\\log_{()}(())"
    "\\frac{1}{x}" -> "\\frac{(1)}{()}"
    "\\bmod" -> "\\bmod"
    "\\left|x\\right|" -> "\\left|()\\right|"
    "x!" -> "!"

    "C", "⌫", "◀", "▶", "▲", "▼", "ans", "+/-", "=" -> ""

    else -> key
}

private fun insertionCursorDelta(inserted: String): Int {
    inserted.indexOf("{}").takeIf { it >= 0 }?.let { return it + 1 }
    inserted.indexOf("()").takeIf { it >= 0 }?.let { return it + 1 }
    inserted.indexOf("[]").takeIf { it >= 0 }?.let { return it + 1 }
    return inserted.length
}

fun toLatex(input: String, cursorIndex: Int = -1): String {
    val withCursor = if (cursorIndex in 0..input.length) {
        input.substring(0, cursorIndex) + "█" + input.substring(cursorIndex)
    } else input

    val processed = withCursor
        .replace("█", "{\\color{red}|}")

    return processed
}
