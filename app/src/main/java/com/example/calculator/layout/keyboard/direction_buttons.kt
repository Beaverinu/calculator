package com.example.calculator.layout.keyboard

import com.example.calculator.layout.keyboard.findMatchingCloseBrace
import com.example.calculator.layout.keyboard.findMatchingCloseBracket
import com.example.calculator.layout.keyboard.findMatchingCloseParen
import kotlin.math.max
import kotlin.math.min

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

    // Superscript/subscript ^{()} or _{( )}
    if (text[pos] == '^' || text[pos] == '_') {
        if (text[pos] == '_' && pos >= 4 && text.substring(0, pos).trimEnd().endsWith("\\log")) {
            // Let the \log logic below handle it
        } else {
            var i = pos + 1
            while (i < len && text[i].isWhitespace()) i++
            if (i < len && text[i] == '{') {
                val closeBrace = findMatchingCloseBrace(text, i) ?: return null
                val braceContent = text.substring(i + 1, closeBrace)
                // Skip if it's a fixed value (doesn't contain parentheses)
                if (braceContent.contains('(')) {
                    val argStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
                    return FunctionCursorMatch(FunctionCursorKind.SIMPLE, pos, argStart, closeBrace + 1)
                }
            }
        }
    }

    if (text.startsWith("\\sqrt", pos)) {
        var i = pos + 5
        while (i < len && text[i].isWhitespace()) i++
        var bracketStart = -1
        var bracketEnd = -1
        if (i < len && text[i] == '[') {
            val closeBracket = findMatchingCloseBracket(text, i) ?: return null
            val bracketContent = text.substring(i + 1, closeBracket)
            // Skip the index slot if it's fixed (like [3]) and doesn't contain ( )
            if (bracketContent.contains('(')) {
                bracketStart = if (i + 1 < len && text[i + 1] == '(') i + 2 else i + 1
                bracketEnd = closeBracket
            }
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

    // Skip \times , \le , \ge , \pi  if to the left
    val tokens = listOf("\\times ", "\\le ", "\\ge ", "\\pi ", "\\bmod ")
    for (tok in tokens) {
        if (pos >= tok.length && text.regionMatches(pos - tok.length, tok, 0, tok.length)) {
            return (pos - tok.length).coerceAtLeast(0)
        }
    }

    var match: FunctionCursorMatch? = null
    var s = (pos - 1).coerceAtMost(len - 1)
    while (s >= 0) {
        if (text[s] == '\\' || text[s] == '^' || text[s] == '_') {
            val m = matchFunctionCursorAt(text, s)
            if (m != null && pos > m.start && pos <= m.endIndex) {
                match = m
                break
            }
        }
        if (pos - s > 100) break 
        s--
    }

    if (match == null) return (pos - 1).coerceIn(0, len)

    return when (pos) {
        match.endIndex -> {
            val closeParen = if (match.argumentIndex > 0 && text[match.argumentIndex - 1] == '(') {
                findMatchingCloseParen(text, match.argumentIndex - 1)
            } else null
            if (closeParen != null) closeParen else match.argumentIndex
        }
        match.argumentIndex -> {
            if (match.baseStart != -1) {
                val closeParenBase = if (match.baseStart > 0 && text[match.baseStart - 1] == '(') {
                    findMatchingCloseParen(text, match.baseStart - 1)
                } else null
                if (closeParenBase != null) closeParenBase else match.baseStart
            } else {
                if ((text[match.start] == '^' || text[match.start] == '_') && match.start > 0 && text[match.start - 1] == ')') {
                    match.start - 1
                } else {
                    match.start
                }
            }
        }
        match.baseStart -> {
            match.start
        }
        else -> {
            var target = pos - 1
            while (target > match.start && (text[target] == '(' || text[target] == '{' || text[target] == '[')) {
                target--
            }
            target
        }
    }.coerceIn(0, len)
}

fun moveCursorRightStructurally(text: String, pos: Int): Int {
    val len = text.length
    if (pos >= len) return len

    // Skip \times , \le , \ge , \pi  if to the right
    val tokens = listOf("\\times ", "\\le ", "\\ge ", "\\pi ", "\\bmod ")
    for (tok in tokens) {
        if (text.startsWith(tok, pos)) {
            return (pos + tok.length).coerceAtMost(len)
        }
    }

    val mExact = matchFunctionCursorAt(text, pos)
    if (mExact != null) {
        return if (mExact.baseStart != -1) mExact.baseStart else mExact.argumentIndex
    }

    var match: FunctionCursorMatch? = null
    var s = (pos - 1).coerceAtLeast(0)
    while (s >= 0) {
        if (text[s] == '\\' || text[s] == '^' || text[s] == '_') {
            val m = matchFunctionCursorAt(text, s)
            if (m != null && pos >= m.start && pos < m.endIndex) {
                match = m
                break
            }
        }
        if (pos - s > 100) break
        s--
    }

    if (match == null) {
        val nextPos = pos + 1
        if (nextPos < len) {
            val mNext = matchFunctionCursorAt(text, nextPos)
            if (mNext != null && (text[pos] == ')' || text[pos] == '}' || text[pos] == ']')) {
                return if (mNext.baseStart != -1) mNext.baseStart else mNext.argumentIndex
            }
        }
        return (pos + 1).coerceAtMost(len)
    }

    val argEnd = if (match.argumentIndex > 0 && text[match.argumentIndex - 1] == '(') {
        findMatchingCloseParen(text, match.argumentIndex - 1) ?: (match.endIndex - 1)
    } else {
        match.endIndex - 1
    }
    val baseEnd = if (match.baseStart != -1) {
        if (match.baseStart > 0 && text[match.baseStart - 1] == '(') {
            findMatchingCloseParen(text, match.baseStart - 1) ?: match.baseEnd
        } else {
            match.baseEnd
        }
    } else -1

    return when (pos) {
        baseEnd -> {
            match.argumentIndex
        }
        argEnd -> {
            match.endIndex
        }
        else -> {
            var target = pos + 1
            while (target < match.endIndex && target != argEnd && target != baseEnd && 
                   (text[target] == ')' || text[target] == '}' || text[target] == ']')) {
                target++
            }
            target
        }
    }.coerceIn(0, len)
}
