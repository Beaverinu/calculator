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
    matchFunctionCursorAt(text, start)?.let { match ->
        return match.start to match.endIndex
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
private fun isInverseTrigCallAtOpenParen(text: String, openParen: Int): Int? {
    if (openParen !in text.indices || text[openParen] != '(') return null

    val funcs = listOf("\\sin", "\\cos", "\\tan", "\\sinh", "\\cosh", "\\tanh")

    for (f in funcs) {
        val searchStart = (openParen - f.length).coerceAtLeast(0)
        for (startCandidate in searchStart downTo 0) {
            if (!text.regionMatches(startCandidate, f, 0, f.length)) continue

            val afterFunc = startCandidate + f.length
            val afterExp = skipOptionalExponent(text, afterFunc) ?: continue

            var j = afterExp
            while (j < openParen && text[j].isWhitespace()) j++
            if (j == openParen) return startCandidate
        }
    }
    return null
}
private fun isSimpleFunctionCallAtOpenParen(
    text: String,
    openParen: Int,
    command: String,
): Int? {
    if (openParen !in text.indices || text[openParen] != '(') return null

    val len = text.length
    val searchStart = (openParen - command.length).coerceAtLeast(0)

    for (startCandidate in searchStart downTo 0) {
        if (!text.regionMatches(startCandidate, command, 0, command.length)) continue

        var i = startCandidate + command.length
        while (i < len && text[i].isWhitespace()) i++

        if (command == "\\log" && i < openParen && text[i] == '_') {
            i++
            while (i < len && text[i].isWhitespace()) i++
            if (i >= openParen || text[i] != '{') continue

            val close = findMatchingCloseBrace(text, i) ?: continue
            i = close + 1
            while (i < len && text[i].isWhitespace()) i++
        }

        if (i == openParen) return startCandidate
    }

    return null
}
private enum class FunctionCursorKind {
    SQRT,
    FRAC,
    SIMPLE,
}
private data class FunctionCursorMatch(
    val kind: FunctionCursorKind,
    val start: Int,
    val argumentIndex: Int,
    val endIndex: Int,
)
private fun matchFunctionCursorAt(text: String, pos: Int): FunctionCursorMatch? {
    val len = text.length

    if (text.startsWith("\\sqrt", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '[') {
            val closeBracket = findMatchingCloseBracket(text, i) ?: return null
            i = closeBracket + 1
            while (i < len && text[i].isWhitespace()) i++
        }
        if (i < len && text[i] == '{') {
            val closeBrace = findMatchingCloseBrace(text, i) ?: return null
            return FunctionCursorMatch(FunctionCursorKind.SQRT, pos, i + 1, closeBrace + 1)
        }
    }

    if (text.startsWith("\\frac", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '{') {
            val closeNum = findMatchingCloseBrace(text, i) ?: return null
            i = closeNum + 1
            while (i < len && text[i].isWhitespace()) i++
            if (i < len && text[i] == '{') {
                val closeDen = findMatchingCloseBrace(text, i) ?: return null
                return FunctionCursorMatch(FunctionCursorKind.FRAC, pos, i + 1, closeDen + 1)
            }
        }
    }

    val simpleFuncs = listOf("\\sin", "\\cos", "\\tan", "\\sinh", "\\cosh", "\\tanh", "\\ln", "\\exp", "\\log")
    for (command in simpleFuncs) {
        if (!text.startsWith(command, pos)) continue

        val afterFunc = pos + command.length
        val afterExp = skipOptionalExponent(text, afterFunc) ?: return null

        if (afterExp < len && text[afterExp] == '(') {
            val close = findMatchingCloseParen(text, afterExp) ?: return null
            return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, afterExp + 1, close + 1)
        }
        if (afterExp < len && text[afterExp] == '{') {
            val close = findMatchingCloseBrace(text, afterExp) ?: return null
            return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, afterExp + 1, close + 1)
        }
    }

    return null
}
private fun matchFunctionAtOpenParen(text: String, openParen: Int): FunctionCursorMatch? {
    if (openParen !in text.indices || text[openParen] != '(') return null

    val simpleCommands = listOf("\\ln", "\\sin", "\\cos", "\\tan", "\\sinh", "\\cosh", "\\tanh", "\\exp", "\\log")
    for (cmd in simpleCommands) {
        val start = isSimpleFunctionCallAtOpenParen(text, openParen, cmd)
        if (start != null) return FunctionCursorMatch(FunctionCursorKind.SIMPLE, start, openParen + 1, openParen + 1)
    }

    return null
}
fun moveCursorLeftStructurally(text: String, pos: Int): Int {
    if (pos <= 0) return 0
    val len = text.length

    if (pos > 0 && text[pos - 1] == ')') {
        val close = pos - 1
        val open = findMatchingOpenParen(text, close)
        if (open != null) {
            if (matchFunctionAtOpenParen(text, open) != null) {
                return (open + 1).coerceIn(0, len)
            }
        }
    }

    if (pos > 0 && text[pos - 1] == '(') {
        val open = pos - 1
        val match = matchFunctionAtOpenParen(text, open)
        if (match != null) {
            return match.start.coerceIn(0, len)
        }
    }

    if (pos < len && text[pos] == ')') {
        val close = pos
        val open = findMatchingOpenParen(text, close)
        if (open != null) {
            val match = matchFunctionAtOpenParen(text, open)
            if (match != null) {
                return match.start.coerceIn(0, len)
            }
        }
    }

    if (pos > 0 && text[pos - 1] == ')') {
        val close = pos - 1
        val open = findMatchingOpenParen(text, close)
        if (open != null) {
            val start = isInverseTrigCallAtOpenParen(text, open)
            if (start != null) {
                return (open + 1).coerceIn(0, len)
            }
        }
    }
    if (pos > 0 && text[pos - 1] == '(') {
        val open = pos - 1
        val start = isInverseTrigCallAtOpenParen(text, open)
        if (start != null) {
            return start.coerceIn(0, len)
        }
    }

    if (pos < len && text[pos] == ')') {
        val close = pos
        val open = findMatchingOpenParen(text, close)
        if (open != null) {
            val start = isInverseTrigCallAtOpenParen(text, open)
            if (start != null) {
                return start.coerceIn(0, len)
            }
        }
    }

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

    if (text.startsWith("\\sqrt", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '[') {
            val closeBracket = findMatchingCloseBracket(text, i)
            if (closeBracket != null) i = closeBracket + 1
        }
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '{') return (i + 1).coerceAtMost(len)
    }

    if (text.startsWith("\\frac", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '{') return (i + 1).coerceAtMost(len)
    }

    matchFunctionCursorAt(text, pos)?.let { match ->
        return match.argumentIndex.coerceAtMost(len)
    }

    if (text[pos] == '\\') {
        var end = pos + 1
        while (end < len && text[end].isLetter()) end++
        if (end > pos + 1) return end
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
            matchFunctionCursorAt(text, pos)?.let { match ->
                return match.endIndex.coerceAtMost(len)
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
private fun skipOptionalExponent(text: String, indexAfterFunc: Int): Int? {
    val len = text.length
    var i = indexAfterFunc

    while (i < len && text[i].isWhitespace()) i++

    if (i >= len || text[i] != '^') return i  // no exponent => unchanged

    i++
    while (i < len && text[i].isWhitespace()) i++
    if (i >= len) return null

    if (text[i] == '{') {
        val close = findMatchingCloseBrace(text, i) ?: return null
        return close + 1
    }

    var end = i
    if (text[end] == '+' || text[end] == '-') end++
    while (end < len && (text[end].isLetterOrDigit() || text[end] == '.' || text[end] == '_')) {
        end++
    }
    return if (end > i) end else (i + 1).coerceAtMost(len)
}
private fun findMatchingOpenParen(text: String, close: Int): Int? {
    if (close !in text.indices || text[close] != ')') return null
    var depth = 1
    for (i in close - 1 downTo 0) {
        when (text[i]) {
            ')' -> depth++
            '(' -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return null
}
