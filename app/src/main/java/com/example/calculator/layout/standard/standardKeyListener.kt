package com.example.calculator.layout.standard

import androidx.compose.ui.text.TextRange
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import com.example.calculator.parser.previous_ans

fun pressed_key(current_key_pressed: String) {
    val state = current_text_state
    
    when (current_key_pressed) {
        "2ⁿᵈ" -> current_keyboard_in_use = 2
        "1ˢᵗ" -> current_keyboard_in_use = 1
        "C" -> state.edit { replace(0, length, "0") }
        "▲" -> {
            state.edit { selection = TextRange(0) }
        }
        "◀" -> {
            state.edit {
                val currentPos = selection.start
                if (currentPos > 0) {
                    selection = TextRange(currentPos - 1)
                }
            }
        }
        "▶" -> {
            state.edit {
                val currentPos = selection.start
                if (currentPos < length) {
                    selection = TextRange(currentPos + 1)
                }
            }
        }
        "▼" -> {
            state.edit { selection = TextRange(length) }
        }
        "+/-" -> {
            state.edit {
                val newText = plus_minus(toString())
                replace(0, length, newText)
                selection = TextRange(length)
            }
        }
        "ans" -> {
            state.edit {
                insert(length, previous_ans.value)
            }
        }
        "⌫" -> state.edit {
            val pos = selection.start
            if (pos > 0) {
                delete(pos - 1, pos)
                selection = TextRange(pos - 1)
            }
            if (length == 0) insert(0, "0")
        }
        else -> {
            state.edit {
                val text = toString()
                
                if (text == "0" && current_key_pressed != ".") {
                    replace(0, 1, "")
                }
                
                // Prepare the text to append (e.g. adding brackets)
                val toAppendBase = if ( current_key_pressed.contains("sin") ||
                                        current_key_pressed.contains("cos") ||
                                        current_key_pressed.contains("tan") ||
                                        current_key_pressed.contains("log") ||
                                        current_key_pressed.contains("ln")) {
                    
                    if (current_key_pressed == "logᵧx") {
                         "logᵧ(x"
                    }
                    else if (current_key_pressed.contains("x") && !current_key_pressed.contains("exp")) {
                        current_key_pressed.replace("x", "(") + ")"
                    }
                    else {
                        current_key_pressed + "("
                    }
                } else {
                    current_key_pressed
                }

                val xIndex = text.indexOf('x')
                val yIndex = text.indexOf('Y')
                val xSupIndex = text.indexOf('ˣ')
                val ySupIndex = text.indexOf('ʸ')
                val basePlaceholderIndex = text.indexOf('ᵧ')
                
                val targetIndex = listOf(xIndex, yIndex, xSupIndex, ySupIndex, basePlaceholderIndex)
                    .filter { it != -1 }
                    .minOrNull()

                if (targetIndex != null) {
                    val charAtTarget = text[targetIndex]
                    val replacement = when (charAtTarget) {
                        'ˣ', 'ʸ' -> toSuperscript(toAppendBase)
                        'ᵧ' -> toSubscript(toAppendBase)
                        else -> toAppendBase
                    }
                    replace(targetIndex, targetIndex + 1, replacement)
                    selection = TextRange(targetIndex + replacement.length)
                } else {
                    // Smart append: handle exponents and subscripts
                    val toAppend = if (text.lastOrNull()?.isSuperscript() == true && current_key_pressed.all { it.isDigit() }) {
                        toSuperscript(toAppendBase)
                    } else if (text.lastOrNull()?.isSubscript() == true && current_key_pressed.all { it.isDigit() }) {
                        toSubscript(toAppendBase)
                    } else {
                        toAppendBase
                    }
                    val pos = selection.start
                    insert(pos, toAppend)
                    selection = TextRange(pos + toAppend.length)
                }
            }
        }
    }
}

fun toSuperscript(input: String): String {
    val map = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        'x' to 'ˣ', 'y' to 'ʸ', '+' to '⁺', '-' to '⁻', '(' to '⁽', ')' to '⁾'
    )
    return input.map { map[it] ?: it }.joinToString("")
}

fun toSubscript(input: String): String {
    val map = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        'x' to 'ₓ', 'y' to 'ᵧ', '+' to '₊', '-' to '₋', '(' to '₍', ')' to '₎'
    )
    return input.map { map[it] ?: it }.joinToString("")
}

fun Char.isSuperscript(): Boolean = this in "⁰¹²³⁴⁵⁶⁷⁸⁹ˣʸ⁺⁻⁽⁾"
fun Char.isSubscript(): Boolean = this in "₀₁₂₃₄₅₆₇₈₉ₓᵧ₊₋₍₎"

fun plus_minus(input: String): String {
    val lastPlus = input.lastIndexOf('+')
    val lastMinus = input.lastIndexOf('-')
    val lastSignIndex = maxOf(lastPlus, lastMinus)

    if (lastSignIndex != -1) {
        val flipped = if (input[lastSignIndex] == '+') '-' else '+'
        return buildString(input.length) {
            append(input, 0, lastSignIndex)
            append(flipped)
            append(input, lastSignIndex + 1, input.length)
        }
    }

    var end = input.length - 1
    while (end >= 0 && !(input[end].isDigit() || input[end] == '.')) end--
    if (end < 0) return input

    var start = end
    while (start >= 0 && (input[start].isDigit() || input[start] == '.')) start--
    start++

    return buildString(input.length + 1) {
        append(input, 0, start)
        append('-')
        append(input, start, input.length)
    }
}
