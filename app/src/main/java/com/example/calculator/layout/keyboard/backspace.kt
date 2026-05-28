package com.example.calculator.layout.keyboard

fun backspaceDeleteRange(text: String, cursor: Int): Pair<Int, Int> {
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


