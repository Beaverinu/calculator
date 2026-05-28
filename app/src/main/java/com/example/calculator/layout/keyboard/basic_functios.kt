package com.example.calculator.layout.keyboard


fun findMatchingOpenBrace(text: String, closeIndex: Int): Int? {
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
fun findMatchingOpenBracket(text: String, closeIndex: Int): Int? {
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



