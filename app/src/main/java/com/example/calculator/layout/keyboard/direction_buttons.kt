package com.example.calculator.layout.keyboard

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
fun moveCursorLeftStructurally(text: String, pos: Int): Int {
    if (pos <= 0) return 0
    val len = text.length

    if (pos < len && text[pos] == '{' && isAfterLatexCommandAt(text, pos, "\\sqrt")) {
        return (pos + 1).coerceAtMost(len)
    }
    if (pos < len && text[pos] == '[' && isAfterLatexCommandAt(text, pos, "\\sqrt")) {
        return (pos + 1).coerceAtMost(len)
    }
    if (pos < len && text[pos] == '{' && isAfterLatexCommandAt(text, pos, "\\frac")) {
        return (pos + 1).coerceAtMost(len)
    }

    if (pos < len && text[pos] == '{' && pos > 0 && (text[pos - 1] == '^' || text[pos - 1] == '_')) {
        return (pos + 1).coerceAtMost(len)
    }
    if (pos >= 2 && text[pos - 1] == '{' && text[pos - 2] == '}') {
        val numClose = pos - 2
        val numOpen = findMatchingOpenBrace(text, numClose)
        if (numOpen != null) {
            var j = numOpen - 1
            while (j >= 0 && text[j].isWhitespace()) j--
            val fracStart = j - ("\\frac".length - 1)
            if (fracStart >= 0 && text.regionMatches(fracStart, "\\frac", 0, 5)) {
                return numClose
            }
        }
    }
    if (pos > 0 && text[pos - 1] == '{') {
        val openBrace = pos - 1
        if (openBrace > 0 && (text[openBrace - 1] == '^' || text[openBrace - 1] == '_')) {
            return (openBrace - 1).coerceAtLeast(0)
        }
        run {
            var j = openBrace - 1
            while (j >= 0 && text[j].isWhitespace()) j--

            if (j >= 0 && text[j] == ']') {
                val indexOpen = findMatchingOpenBracket(text, j)
                if (indexOpen != null) {
                    var k = indexOpen - 1
                    while (k >= 0 && text[k].isWhitespace()) k--
                    val sqrtStart = k - ("\\sqrt".length - 1)
                    if (sqrtStart >= 0 && text.regionMatches(sqrtStart, "\\sqrt", 0, 5)) {
                        return sqrtStart
                    }
                }
            } else {
                val sqrtStart = j - ("\\sqrt".length - 1)
                if (sqrtStart >= 0 && text.regionMatches(sqrtStart, "\\sqrt", 0, 5)) {
                    return sqrtStart
                }
            }
        }
        run {
            var j = openBrace - 1
            while (j >= 0 && text[j].isWhitespace()) j--
            val fracStart = j - ("\\frac".length - 1)
            if (fracStart >= 0 && text.regionMatches(fracStart, "\\frac", 0, 5)) {
                return fracStart
            }
        }
    }
    if (pos > 0 && (text[pos - 1] == '{' || text[pos - 1] == '[' || text[pos - 1] == '(')) {
        return pos - 1
    }
    if (pos > 0 && (text[pos - 1] == '}' || text[pos - 1] == ']' || text[pos - 1] == ')')) {
        return pos - 1
    }
    backspaceDeleteRange(text, pos).let { (from, _) ->
        if (from < pos) return from
    }

    return (pos - 1).coerceIn(0, len)
}
fun moveCursorRightStructurally(text: String, pos: Int): Int {
    val len = text.length
    if (pos >= len) return len
    if (isAfterLatexCommandAt(text, pos, "\\sqrt")) {
        if (pos < len && (text[pos] == '[' || text[pos] == '{')) return (pos + 1).coerceAtMost(len)
    }

    if (isAfterLatexCommandAt(text, pos, "\\frac")) {
        if (pos < len && text[pos] == '{') return (pos + 1).coerceAtMost(len)
    }
    if (pos < len && text[pos] == '}' && (pos + 1) < len && text[pos + 1] == '{') {
        val numClose = pos
        val numOpen = findMatchingOpenBrace(text, numClose)
        if (numOpen != null) {
            var j = numOpen - 1
            while (j >= 0 && text[j].isWhitespace()) j--
            val fracStart = j - ("\\frac".length - 1)
            if (fracStart >= 0 && text.regionMatches(fracStart, "\\frac", 0, 5)) {
                return (pos + 2).coerceAtMost(len) // jump inside denominator
            }
        }
    }
    if (pos < len && text[pos] == '{' && pos > 0 && (text[pos - 1] == '^' || text[pos - 1] == '_')) {
        return (pos + 1).coerceAtMost(len)
    }
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
            if (text[pos] == '^' || text[pos] == '_') {
                if (pos + 1 < len && text[pos + 1] == '{') return (pos + 2).coerceAtMost(len)
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
private fun isAfterLatexCommandAt(text: String, pos: Int, command: String): Boolean {
    val start = pos - command.length
    return start >= 0 && text.regionMatches(start, command, 0, command.length)
}



