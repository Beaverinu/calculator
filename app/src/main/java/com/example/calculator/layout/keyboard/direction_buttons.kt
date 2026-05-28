package com.example.calculator.layout.keyboard

private fun findMatchingCloseBrace(text: String, open: Int): Int? {
    if (open !in text.indices || text[open] != '{') return null
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
    if (open !in text.indices || text[open] != '[') return null
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
    if (open !in text.indices || text[open] != '(') return null
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
    val baseStart: Int = -1,
    val baseEnd: Int = -1,
)

private fun matchFunctionCursorAt(text: String, pos: Int): FunctionCursorMatch? {
    val len = text.length
    if (pos !in text.indices) return null

    // Check for superscript/subscript structure ^{} or _{}
    if (text[pos] == '^' || text[pos] == '_') {
        var i = pos + 1
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '{') {
            val closeBrace = findMatchingCloseBrace(text, i) ?: return null
            return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, i + 1, closeBrace + 1)
        }
    }

    if (text.startsWith("\\sqrt", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        var bracketStart = -1
        var bracketEnd = -1
        if (i < len && text[i] == '[') {
            bracketStart = i + 1
            val closeBracket = findMatchingCloseBracket(text, i) ?: return null
            bracketEnd = closeBracket
            i = closeBracket + 1
            while (i < len && text[i].isWhitespace()) i++
        }
        if (i < len && text[i] == '{') {
            val closeBrace = findMatchingCloseBrace(text, i) ?: return null
            return FunctionCursorMatch(FunctionCursorKind.SQRT, pos, i + 1, closeBrace + 1, baseStart = bracketStart, baseEnd = bracketEnd)
        }
    }

    if (text.startsWith("\\frac", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '{') {
            val numOpen = i
            val closeNum = findMatchingCloseBrace(text, i) ?: return null
            i = closeNum + 1
            while (i < len && text[i].isWhitespace()) i++
            if (i < len && text[i] == '{') {
                val closeDen = findMatchingCloseBrace(text, i) ?: return null
                return FunctionCursorMatch(FunctionCursorKind.FRAC, pos, i + 1, closeDen + 1, baseStart = numOpen + 1, baseEnd = closeNum)
            }
        }
    }

    if (text.startsWith("\\log", pos)) {
        var i = pos + 4
        while (i < len && text[i].isWhitespace()) i++
        var baseStart = -1
        var baseEnd = -1
        if (i < len && text[i] == '_') {
            i++
            while (i < len && text[i].isWhitespace()) i++
            if (i < len && text[i] == '{') {
                val closeBase = findMatchingCloseBrace(text, i) ?: return null
                baseStart = i + 1
                baseEnd = closeBase
                i = closeBase + 1
                while (i < len && text[i].isWhitespace()) i++
            }
        }

        if (i < len && text[i] == '(') {
            val close = findMatchingCloseParen(text, i) ?: return null
            val argumentIndex = i + 1
            return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, argumentIndex, close + 1, baseStart, baseEnd)
        }
    }

    val simpleFuncs = listOf(
        "\\sin^{-1}", "\\cos^{-1}", "\\tan^{-1}",
        "\\sinh^{-1}", "\\cosh^{-1}", "\\tanh^{-1}",
        "\\sin", "\\cos", "\\tan", "\\sinh", "\\cosh", "\\tanh",
        "\\ln", "\\exp"
    )
    for (command in simpleFuncs) {
        if (!text.startsWith(command, pos)) continue

        val afterFunc = pos + command.length
        var i = afterFunc
        while (i < len && text[i].isWhitespace()) i++

        if (i < len && text[i] == '(') {
            val close = findMatchingCloseParen(text, i) ?: return null
            return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, i + 1, close + 1)
        }
    }

    return null
}

fun moveCursorLeftStructurally(text: String, pos: Int): Int {
    if (pos <= 0) return 0
    val len = text.length

    var s = (pos - 1).coerceAtMost(len - 1)
    while (s >= 0) {
        if (text[s] == '\\' || text[s] == '^' || text[s] == '_') {
            val match = matchFunctionCursorAt(text, s)
            if (match != null && pos > match.start && pos <= match.endIndex) {
                if (match.kind == FunctionCursorKind.FRAC || match.kind == FunctionCursorKind.SQRT) {
                    return when {
                        pos >= match.endIndex -> (match.endIndex - 1).coerceAtLeast(0)
                        pos > match.argumentIndex -> (pos - 1).coerceAtLeast(0)
                        match.baseEnd != -1 && pos > match.baseEnd -> match.baseEnd.coerceAtLeast(0)
                        match.baseStart != -1 && pos > match.baseStart -> (pos - 1).coerceAtLeast(0)
                        else -> match.start.coerceIn(0, len)
                    }
                }
                if ((text[match.start] == '^' || text[match.start] == '_') && match.baseStart == -1) {
                    return when {
                        pos >= match.endIndex -> (match.endIndex - 1).coerceAtLeast(0)
                        pos > match.argumentIndex -> (pos - 1).coerceAtLeast(0)
                        else -> moveCursorLeftStructurally(text, match.start)
                    }
                }
                return when {
                    pos > match.argumentIndex -> match.argumentIndex
                    match.baseStart != -1 && pos > match.baseStart -> match.baseStart
                    else -> match.start
                }.coerceIn(0, len)
            }
        }
        if (pos - s > 30) break 
        s--
    }

    return (pos - 1).coerceIn(0, len)
}

fun moveCursorRightStructurally(text: String, pos: Int): Int {
    val len = text.length
    if (pos >= len) return len

    if (text[pos] == '}') {
        var i = pos + 1
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && (text[i] == '^' || text[i] == '_')) {
            val match = matchFunctionCursorAt(text, i)
            if (match != null && match.baseStart == -1) {
                return match.argumentIndex
            }
        }
    }

    var s = pos
    while (s >= 0) {
        if (text[s] == '\\' || text[s] == '^' || text[s] == '_') {
            val match = matchFunctionCursorAt(text, s)
            if (match != null && pos >= match.start && pos < match.endIndex) {
                if (match.kind == FunctionCursorKind.FRAC || match.kind == FunctionCursorKind.SQRT) {
                    return when {
                        match.baseStart != -1 && pos < match.baseStart -> match.baseStart
                        match.baseStart != -1 && pos < match.baseEnd -> (pos + 1).coerceAtMost(len)
                        pos < match.argumentIndex -> match.argumentIndex
                        pos < match.endIndex - 1 -> (pos + 1).coerceAtMost(len)
                        else -> match.endIndex.coerceIn(0, len)
                    }
                }
                if ((text[match.start] == '^' || text[match.start] == '_') && match.baseStart == -1) {
                    return when {
                        pos < match.argumentIndex -> match.argumentIndex
                        pos < match.endIndex - 1 -> (pos + 1).coerceAtMost(len)
                        else -> match.endIndex.coerceIn(0, len)
                    }
                }
                return when {
                    pos < match.baseStart && match.baseStart != -1 -> match.baseStart
                    pos < match.argumentIndex -> match.argumentIndex
                    else -> match.endIndex
                }.coerceIn(0, len)
            }
        }
        if (pos - s > 30) break
        s--
    }

    return (pos + 1).coerceAtMost(len)
}
