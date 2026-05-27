package com.example.calculator.layout.standard

import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.example.calculator.parser.previous_ans
import kotlin.math.max
import kotlin.math.min

fun pressed_key(current_key_pressed: String) {
    val state = current_text_state

    when (current_key_pressed) {
        "\\(2^{nd})" -> current_keyboard_in_use = 2
        "\\(1^{st})" -> current_keyboard_in_use = 1

        "C" -> state.edit {
            replace(0, length, "0")
            selection = TextRange(1) // after "0"
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

        else -> state.edit {
            var text = toString()
            var pos = selection.start.coerceIn(0, length)

            // Overwrite placeholder if exists at cursor
            if (pos < length && (text[pos] == 'x' || text[pos] == 'y')) {
                delete(pos, pos + 1)
                text = toString()
            }

            if (text == "0" && current_key_pressed != ".") {
                replace(0, 1, "")
                pos = selection.start.coerceIn(0, length)
            }

            val insertion = keyToLatex(current_key_pressed)
            if (insertion.isEmpty()) return@edit

            insert(pos, insertion)

            val delta = insertionCursorDelta(insertion)
            selection = TextRange((pos + delta).coerceIn(0, length))
        }
    }
    if(current_keyboard_in_use == 2 && current_key_pressed != "\\(2^{nd})"){
        current_keyboard_in_use = 1
    }
}

private fun keyToLatex(key: String): String = when (key) {
    "\\(2^{nd})", "\\(1^{st})" -> ""
    "\\pi", "π" -> "\\pi"
    "\\div", "÷" -> "\\div"
    "\\times", "×" -> "\\times"
    "\\sin" -> "\\sin(x)"
    "\\cos" -> "\\cos(x)"
    "\\tan" -> "\\tan(x)"
    "\\sinh" -> "\\sinh(x)"
    "\\cosh" -> "\\cosh(x)"
    "\\tanh" -> "\\tanh(x)"
    "\\log" -> "\\log(x)"
    "\\ln" -> "\\ln(x)"
    "\\exp" -> "\\exp(x)"
    "\\(sin^{-1})" -> "\\sin^{-1}(x)"
    "\\(cos^{-1})" -> "\\cos^{-1}(x)"
    "\\(tan^{-1})" -> "\\tan^{-1}(x)"
    "\\(sinh^{-1})" -> "\\sinh^{-1}(x)"
    "\\(cosh^{-1})" -> "\\cosh^{-1}(x)"
    "\\(tanh^{-1})" -> "\\tanh^{-1}(x)"
    "\\(x^{2})" -> "^{2}"
    "\\(x^{3})" -> "^{3}"
    "\\(x^{y})" -> "^{y}"
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

    "C", "⌫", "◀", "▶", "▲", "▼", "ans", "+/-", "=" -> ""

    else -> key
}

private fun insertionCursorDelta(inserted: String): Int {
    val x = inserted.indexOf('x')
    val y = inserted.indexOf('y')
    if (x != -1 && y != -1) return Math.min(x, y)
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

//backspace
private fun backspaceDeleteRange(text: String, cursor: Int): Pair<Int, Int> {
    val end = cursor.coerceIn(0, text.length)
    if (end == 0) return 0 to 0

    findSqrtRangeEndingAt(text, end)?.let { return it }
    findFracRangeEndingAt(text, end)?.let { return it }
    findScriptGroupEndingAt(text, end)?.let { return it }
    findCommandOrCommandWithOpenerEndingAt(text, end)?.let { return it }

    val fixedTokens = listOf("\\left|", "\\right|")
    for (tok in fixedTokens) {
        if (end >= tok.length && text.regionMatches(end - tok.length, tok, 0, tok.length)) {
            return (end - tok.length) to end
        }
    }
    return (end - 1) to end
}
private fun forwardDeleteRange(text: String, cursor: Int): Pair<Int, Int>? {
    val len = text.length
    val start = cursor.coerceIn(0, len)
    if (start >= len) return null

    if (text.startsWith("\\sqrt", start)) {
        var i = start + 5
        if (i < len && text[i] == '[') {
            val closeBracket = findMatchingCloseBracket(text, i) ?: return null
            i = closeBracket + 1
        }
        if (i < len && text[i] == '{') {
            val closeBrace = findMatchingCloseBrace(text, i) ?: return null
            return start to (closeBrace + 1)
        }
    }
    if (text.startsWith("\\frac", start)) {
        var i = start + 5
        if (i < len && text[i] == '{') {
            val closeNum = findMatchingCloseBrace(text, i) ?: return null
            i = closeNum + 1
            if (i < len && text[i] == '{') {
                val closeDen = findMatchingCloseBrace(text, i) ?: return null
                return start to (closeDen + 1)
            }
        }
    }
    if (start + 1 < len && (text[start] == '^' || text[start] == '_') && text[start + 1] == '{') {
        val close = findMatchingCloseBrace(text, start + 1) ?: return null
        return start to (close + 1)
    }
    val funcs = listOf("\\sin", "\\cos", "\\tan", "\\log", "\\ln", "\\exp", "\\sinh", "\\cosh", "\\tanh")
    for (f in funcs) {
        if (text.startsWith(f, start) && start + f.length < len && text[start + f.length] == '(') {
            val closeP = findMatchingCloseParen(text, start + f.length) ?: return null
            return start to (closeP + 1)
        }
    }
    return null
}
private fun findSqrtRangeEndingAt(text: String, end: Int): Pair<Int, Int>? {
    if (end <= 0 || text[end - 1] != '}') return null

    val radicandOpen = findMatchingOpenBrace(text, end - 1) ?: return null

    var i = radicandOpen - 1
    while (i >= 0 && text[i].isWhitespace()) i--

    val sqrt = "\\sqrt"
    if (i >= sqrt.length - 1 && text.regionMatches(i - (sqrt.length - 1), sqrt, 0, sqrt.length)) {
        val start = i - (sqrt.length - 1)
        return start to end
    }
    if (i >= 0 && text[i] == ']') {
        val indexOpen = findMatchingOpenBracket(text, i) ?: return null
        var j = indexOpen - 1
        while (j >= 0 && text[j].isWhitespace()) j--

        if (j >= sqrt.length - 1 && text.regionMatches(j - (sqrt.length - 1), sqrt, 0, sqrt.length)) {
            val start = j - (sqrt.length - 1)
            return start to end
        }
    }

    return null
}
private fun findFracRangeEndingAt(text: String, end: Int): Pair<Int, Int>? {
    if (end <= 0 || text[end - 1] != '}') return null

    val denOpen = findMatchingOpenBrace(text, end - 1) ?: return null

    var i = denOpen - 1
    while (i >= 0 && text[i].isWhitespace()) i--
    if (i < 0 || text[i] != '}') return null

    val numClose = i
    val numOpen = findMatchingOpenBrace(text, numClose) ?: return null

    var j = numOpen - 1
    while (j >= 0 && text[j].isWhitespace()) j--

    val frac = "\\frac"
    if (j >= frac.length - 1 && text.regionMatches(j - (frac.length - 1), frac, 0, frac.length)) {
        val start = j - (frac.length - 1)
        return start to end
    }

    return null
}
private fun findScriptGroupEndingAt(text: String, end: Int): Pair<Int, Int>? {
    if (end <= 0 || text[end - 1] != '}') return null

    val open = findMatchingOpenBrace(text, end - 1) ?: return null
    if (open <= 0) return null

    val sig = text[open - 1]
    if (sig != '^' && sig != '_') return null

    return (open - 1) to end
}
private fun findCommandOrCommandWithOpenerEndingAt(text: String, end: Int): Pair<Int, Int>? {
    if (end <= 0) return null

    // \sin( etc
    if (text[end - 1] == '(') {
        val cmd = findLatexCommandEndingAt(text, end - 1) ?: return null
        return cmd.first to end
    }

    val cmd = findLatexCommandEndingAt(text, end) ?: return null
    return cmd
}
private fun findLatexCommandEndingAt(text: String, end: Int): Pair<Int, Int>? {
    val e = end.coerceIn(0, text.length)
    if (e == 0) return null

    var i = e - 1
    if (!text[i].isLetter()) return null

    while (i >= 0 && text[i].isLetter()) i--
    if (i >= 0 && text[i] == '\\') {
        val start = i
        val stop = e
        if (stop - start >= 2) return start to stop
    }
    return null
}
private fun findMatchingOpenBrace(text: String, closeIndex: Int): Int? {
    var depth = 0
    for (i in closeIndex downTo 0) {
        when (text[i]) {
            '}' -> depth++
            '{' -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return null
}
private fun findMatchingOpenBracket(text: String, closeIndex: Int): Int? {
    var depth = 0
    for (i in closeIndex downTo 0) {
        when (text[i]) {
            ']' -> depth++
            '[' -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return null
}

//dir buttons
private fun findMatchingCloseBrace(text: String, open: Int): Int? {
    if (text[open] != '{') return null
    var depth = 1
    for (i in open + 1 until text.length) {
        when (text[i]) {
            '{' -> depth++
            '}' -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return null
}
private fun findMatchingCloseBracket(text: String, open: Int): Int? {
    if (text[open] != '[') return null
    var depth = 1
    for (i in open + 1 until text.length) {
        when (text[i]) {
            '[' -> depth++
            ']' -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return null
}
private fun findMatchingCloseParen(text: String, open: Int): Int? {
    if (text[open] != '(') return null
    var depth = 1
    for (i in open + 1 until text.length) {
        when (text[i]) {
            '(' -> depth++
            ')' -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return null
}
private fun moveCursorLeftStructurally(text: String, pos: Int): Int {
    if (pos == 0) return 0

    if (pos > 0 && (text[pos - 1] == '{' || text[pos - 1] == '[' || text[pos - 1] == '(')) {
        return pos - 1
    }
    if (pos > 0 && (text[pos - 1] == '}' || text[pos - 1] == ']' || text[pos - 1] == ')')) {
        return pos - 1
    }
    backspaceDeleteRange(text, pos).let { (from, to) ->
        if (from < pos) return from
    }
    return pos - 1
}
private fun moveCursorRightStructurally(text: String, pos: Int): Int {
    val len = text.length
    if (pos >= len) return len
    if (text[pos] == '{' || text[pos] == '[' || text[pos] == '(') {
        return (pos + 1).coerceAtMost(len)
    }
    if (text[pos] == '}' || text[pos] == ']' || text[pos] == ')') {
        return (pos + 1).coerceAtMost(len)
    }
    forwardDeleteRange(text, pos)?.let { (from, to) ->
        if (from == pos) {
            if (text.startsWith("\\sqrt", pos)) {
                var i = pos + 5
                if (i < len && text[i] == '[') {
                    val closeBracket = findMatchingCloseBracket(text, i)
                    if (closeBracket != null) i = closeBracket + 1
                }
                if (i < len && text[i] == '{') return (i + 1).coerceAtMost(len)
            }
            if (text.startsWith("\\frac", pos)) {
                var i = pos + 5
                if (i < len && text[i] == '{') return (i + 1).coerceAtMost(len)
            }
            val funcs = listOf("\\sin", "\\cos", "\\tan", "\\log", "\\ln", "\\exp", "\\sinh", "\\cosh", "\\tanh")
            for (f in funcs) {
                if (text.startsWith(f, pos)) {
                    val funcEnd = pos + f.length
                    if (funcEnd < len && text[funcEnd] == '(') return (funcEnd + 1).coerceAtMost(len)
                    if (funcEnd < len && text[funcEnd] == '{') return (funcEnd + 1).coerceAtMost(len)
                }
            }
            return to
        }
    }
    return (pos + 1).coerceAtMost(len)
}



