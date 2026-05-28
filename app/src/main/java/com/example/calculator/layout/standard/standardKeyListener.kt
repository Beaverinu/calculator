package com.example.calculator.layout.standard

import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.example.calculator.parser.previous_ans
import com.example.calculator.layout.keyboard.backspaceDeleteRange
import com.example.calculator.layout.keyboard.moveCursorLeftStructurally
import com.example.calculator.layout.keyboard.moveCursorRightStructurally
import kotlin.math.min

fun pressed_key(current_key_pressed: String) {
    val state = current_text_state

    when (current_key_pressed) {
        "\\(2^{nd})" -> current_keyboard_in_use = 2
        "\\(1^{st})" -> current_keyboard_in_use = 1
        "C" -> state.edit {
            replace(0, length, "0")
            selection = TextRange(1)
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
        "▲", "▼" -> Unit
        "⌫" -> state.edit {
            val pos = selection.start.coerceIn(0, length)
            if (pos > 0) {
                val text = toString()
                val (from, to) = backspaceDeleteRange(text, pos)
                delete(from, to)
                selection = TextRange(from.coerceIn(0, length))
            }
            if (length == 0) {
                insert(0, "0")
                selection = TextRange(1)
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
            while (start > 0 && (text[start - 1].isDigit() || text[start - 1] == '.')) start--

            if (start > 0 && (text[start - 1] == '-' || text[start - 1] == '+')) {
                val newSign = if (text[start - 1] == '-') "+" else "-"
                replace(start - 1, start, newSign)
                selection = TextRange(cur.coerceIn(0, length))
            } else {
                insert(start, "-")
                selection = TextRange((cur + 1).coerceIn(0, length))
            }
        }
        "\\exp" -> Unit
        else -> state.edit {
            var text = toString()
            var pos = selection.start.coerceIn(0, length)

            if (pos < length && (text[pos] == 'x' || text[pos] == 'y')) {
                delete(pos, pos + 1)
                text = toString()
            }

            if (text == "0" && current_key_pressed != ".") {
                replace(0, 1, "")
                pos = selection.start.coerceIn(0, length)
            }

            if (current_key_pressed == "\\(x^{2})" || current_key_pressed == "\\(x^{3})" || current_key_pressed == "\\(x^{y})") {
                val exponent = when (current_key_pressed) {
                    "\\(x^{2})" -> "2"
                    "\\(x^{3})" -> "3"
                    else -> "y"
                }

                val attachToExistingBase = pos > 0 && text[pos - 1].isDigit()
                val insertion = if (attachToExistingBase) "^{$exponent}" else "x^{$exponent}"

                insert(pos, insertion)
                val delta = if (attachToExistingBase) insertion.length else insertionCursorDelta(insertion)
                selection = TextRange((pos + delta).coerceIn(0, length))
                return@edit
            }

            val insertion = keyToLatex(current_key_pressed)
            if (insertion.isEmpty()) return@edit

            insert(pos, insertion)

            val delta = insertionCursorDelta(insertion)
            selection = TextRange((pos + delta).coerceIn(0, length))
        }
    }
    if (current_keyboard_in_use == 2 && current_key_pressed != "\\(2^{nd})") {
        current_keyboard_in_use = 1
    }
}

private fun keyToLatex(key: String): String = when (key) {
    "\\(2^{nd})", "\\(1^{st})" -> ""
    "\\pi", "π" -> "\\pi"
    "\\div", "÷" -> "\\frac{}{}"
    "\\times", "×" -> "\\times"
    "\\sin" -> "\\sin(x)"
    "\\cos" -> "\\cos(x)"
    "\\tan" -> "\\tan(x)"
    "\\sinh" -> "\\sinh(x)"
    "\\cosh" -> "\\cosh(x)"
    "\\tanh" -> "\\tanh(x)"
    "\\log" -> "\\log(x)"
    "\\ln" -> "\\ln(x)"
    "\\(sin^{-1})" -> "\\sin^{-1}(x)"
    "\\(cos^{-1})" -> "\\cos^{-1}(x)"
    "\\(tan^{-1})" -> "\\tan^{-1}(x)"
    "\\(sinh^{-1})" -> "\\sinh^{-1}(x)"
    "\\(cosh^{-1})" -> "\\cosh^{-1}(x)"
    "\\(tanh^{-1})" -> "\\tanh^{-1}(x)"
    "\\(x^{2})" -> "x^{2}"
    "\\(x^{3})" -> "x^{3}"
    "\\(x^{y})" -> "x^{y}"
    "\\(10^{x})" -> "10^{x}"
    "\\(2^{x})" -> "2^{x}"
    "\\(e^{x})" -> "e^{x}"
    "\\sqrt{x}" -> "\\sqrt{x}"
    "\\(^[3])\\sqrt{x}x" -> "\\sqrt[3]{x}"
    "\\(^[])\\sqrt{x}x" -> "\\sqrt[x]{y}"
    "\\(log_{y}x)" -> "\\log_{y}(x)"
    "\\frac{1}{x}" -> "\\frac{1}{x}"
    "\\bmod" -> "\\bmod"
    "\\left|x\\right|" -> "\\left|x\\right|"
    "x!" -> "!"

    "C", "⌫", "◀", "▶", "▲", "▼", "ans", "+/-", "=", "\\exp" -> ""

    else -> key
}

private fun insertionCursorDelta(inserted: String): Int {
    val x = inserted.indexOf('x')
    val y = inserted.indexOf('y')
    if (x != -1 && y != -1) return min(x, y)
    if (x != -1) return x
    if (y != -1) return y

    inserted.indexOf("{}").takeIf { it >= 0 }?.let { return it + 1 }
    inserted.indexOf("()").takeIf { it >= 0 }?.let { return it + 1 }
    inserted.indexOf("[]").takeIf { it >= 0 }?.let { return it + 1 }
    return inserted.length
}

fun toLatex(input: String, cursorIndex: Int = -1): String {
    val withCursor = if (cursorIndex in 0..input.length) {
        input.substring(0, cursorIndex) + "█" + input.substring(cursorIndex)
    } else input

    val withPlaceholders = withCursor
        .replace("█x", "█")
        .replace("x█", "█")
        .replace("█y", "█")
        .replace("y█", "█")
        .replace("x", "\\square")
        .replace("y", "\\square")
        .replace("{}", "{\\square}")
        .replace("[]", "[\\square]")
        .replace("()", "(\\square)")
        .replace("█", "{\\color{red}|}")

    return withPlaceholders
}

