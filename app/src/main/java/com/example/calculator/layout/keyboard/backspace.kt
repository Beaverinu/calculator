package com.example.calculator.layout.keyboard

fun backspaceDeleteRange(text: String, cursor: Int): Pair<Int, Int> {
    val end = cursor.coerceIn(0, text.length)
    if (end <= 0) return 0 to 0

    var s = (end - 1).coerceAtMost(text.length - 1)
    while (s >= 0) {
        if (text[s] == '\\' || text[s] == '^' || text[s] == '_') {

            // Instantly delete \times, \le, \ge, \pi
            val fixedTokens = listOf("\\times", "\\le", "\\ge", "\\pi")
            for (tok in fixedTokens) {
                if (text.startsWith(tok, s)) {
                    if (end > s && end <= s + tok.length) {
                        return s to (s + tok.length)
                    }
                }
            }

            val match = matchFunctionCursorAt(text, s)
            if (match != null && end > match.start && end <= match.endIndex) {

                if (end == match.argumentIndex || (match.baseStart != -1 && end == match.baseStart)) {
                    return match.start to match.endIndex
                }
                if (end == match.endIndex) {
                    val argEmpty = if (match.argumentIndex < text.length && text[match.argumentIndex] == ')') true
                    else match.argumentIndex == match.endIndex - 1

                    val baseEmpty = if (match.baseStart != -1) {
                        if (match.baseStart < text.length && text[match.baseStart] == ')' ) true
                        else if (match.baseStart < text.length && text[match.baseStart] == ']' ) true
                        else match.baseStart == match.baseEnd
                    } else true

                    if (argEmpty && baseEmpty) {
                        return match.start to match.endIndex
                    } else if (!argEmpty) {
                        var deletePos = match.endIndex - 1
                        if (deletePos > 0 && text[deletePos - 1] == ')') deletePos--
                        return (deletePos - 1) to deletePos
                    } else if (match.baseStart != -1 && !baseEmpty) {
                        var deletePos = match.baseEnd
                        if (deletePos > 0 && text[deletePos - 1] == ']') deletePos--
                        if (deletePos > 0 && text[deletePos - 1] == ')') deletePos--
                        return (deletePos - 1) to deletePos
                    }
                    return match.start to match.endIndex
                }

                break
            }
        }
        if (end - s > 100) break
        s--
    }

    return (end - 1) to end
}
