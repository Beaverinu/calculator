package com.example.calculator.layout.keyboard

fun backspaceDeleteRange(text: String, cursor: Int): Pair<Int, Int> {
    val end = cursor.coerceIn(0, text.length)
    if (end <= 0) return 0 to 0

    // Search for a structure that might be affected by backspace at this position
    var s = (end - 1).coerceAtMost(text.length - 1)
    while (s >= 0) {
        if (text[s] == '\\' || text[s] == '^' || text[s] == '_') {
            val match = matchFunctionCursorAt(text, s)
            if (match != null && end > match.start && end <= match.endIndex) {
                
                // 1. "From the front": cursor is at the start of any slot
                if (end == match.argumentIndex || (match.baseStart != -1 && end == match.baseStart)) {
                    return match.start to match.endIndex
                }
                
                // 2. "Until it is empty then delete entire thing": handling at the end of the structure
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
                        // Deleting char-by-char from the argument
                        var deletePos = match.endIndex - 1
                        if (deletePos > 0 && text[deletePos - 1] == ')') deletePos--
                        return (deletePos - 1) to deletePos
                    } else if (match.baseStart != -1 && !baseEmpty) {
                        // Argument is empty, delete char-by-char from the base
                        var deletePos = match.baseEnd
                        if (deletePos > 0 && text[deletePos - 1] == ']') deletePos--
                        if (deletePos > 0 && text[deletePos - 1] == ')') deletePos--
                        return (deletePos - 1) to deletePos
                    }
                    return match.start to match.endIndex
                }
                
                // Otherwise we are in the middle of a slot, use default deletion
                break
            }
        }
        if (end - s > 30) break
        s--
    }

    return (end - 1) to end
}
