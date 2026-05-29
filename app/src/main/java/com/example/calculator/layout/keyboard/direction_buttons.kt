package com.example.calculator.layout.keyboard

enum class FunctionCursorKind {
    SQRT,
    FRAC,
    LOG,
    SIMPLE,
}

data class FunctionCursorMatch(
    val kind: FunctionCursorKind,
    val start: Int,
    val argumentIndex: Int,
    val endIndex: Int,
    val baseStart: Int = -1,
    val baseEnd: Int = -1,
)

fun matchFunctionCursorAt(text: String, pos: Int): FunctionCursorMatch? {
    val len = text.length
    if (pos !in text.indices) return null
    if (text[pos] == '^' || text[pos] == '_') {
        var i = pos + 1
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '{') {
            val closeBrace = findMatchingCloseBrace(text, i) ?: return null
            val argStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
            return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, argStart, closeBrace + 1)
        }
    }

    if (text.startsWith("\\sqrt", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        var bracketStart = -1
        var bracketEnd = -1
        if (i < len && text[i] == '[') {
            val closeBracket = findMatchingCloseBracket(text, i) ?: return null
            bracketStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
            bracketEnd = closeBracket
            i = closeBracket + 1
            while (i < len && text[i].isWhitespace()) i++
        }
        if (i < len && text[i] == '{') {
            val closeBrace = findMatchingCloseBrace(text, i) ?: return null
            val argStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
            return FunctionCursorMatch(FunctionCursorKind.SQRT, pos, argStart, closeBrace + 1, baseStart = bracketStart, baseEnd = bracketEnd)
        }
    }

    if (text.startsWith("\\frac", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        if (i < len && text[i] == '{') {
            val numOpen = i
            val closeNum = findMatchingCloseBrace(text, i) ?: return null
            val numStart = if (numOpen + 1 < len && text[numOpen + 1] == '(') numOpen + 2 else numOpen + 1
            
            i = closeNum + 1
            while (i < len && text[i].isWhitespace()) i++
            if (i < len && text[i] == '{') {
                val closeDen = findMatchingCloseBrace(text, i) ?: return null
                val denStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
                return FunctionCursorMatch(FunctionCursorKind.FRAC, pos, denStart, closeDen + 1, baseStart = numStart, baseEnd = closeNum)
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
                baseStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
                baseEnd = closeBase
                i = closeBase + 1
                while (i < len && text[i].isWhitespace()) i++
            }
        }
        if (i < len && text[i] == '(') {
            val close = findMatchingCloseParen(text, i) ?: return null
            val argumentIndex = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
            return FunctionCursorMatch(FunctionCursorKind.LOG, pos, argumentIndex, close + 1, baseStart, baseEnd)
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
            val argStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
            return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, argStart, close + 1)
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

    val matchExactly = matchFunctionCursorAt(text, pos)
    if (matchExactly != null) {
        return (if (matchExactly.baseStart != -1) matchExactly.baseStart else matchExactly.argumentIndex).coerceIn(0, len)
    }

    var s = (pos - 1).coerceAtLeast(0)
    while (s >= 0) {
        if (text[s] == '\\' || text[s] == '^' || text[s] == '_') {
            val match = matchFunctionCursorAt(text, s)
            if (match != null && pos > match.start && pos < match.endIndex) {
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
