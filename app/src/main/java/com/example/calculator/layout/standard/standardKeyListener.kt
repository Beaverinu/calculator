package com.example.calculator.layout.standard

import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.edit
import androidx.compose.foundation.text.input.insert

fun pressed_key(
    current_key_pressed: String
) {
    val text = current_text_state.text.toString()
    var cursor = cursorIndex.coerceIn(0, text.length)

    when (current_key_pressed) {
        "2ⁿᵈ" -> current_keyboard_in_use = 2
        "1ˢᵗ" -> current_keyboard_in_use = 1
        "C" -> {
            current_text_state.edit {
                replace(0, length, "0")
            }
            cursor = 1
        }
        "▲" -> cursor = 0
        "◀" -> cursor = (cursor - 1).coerceAtLeast(0)
        "▶" -> cursor = (cursor + 1).coerceAtMost(current_text_state.text.length)
        "▼" -> cursor = current_text_state.text.length
        "+/-" -> {
            current_text_state.edit {
                val (updatedText, updatedCursor) = plusMinusAtCursor(
                    input = toString(),
                    cursorIndex = cursor.coerceIn(0, length)
                )
                replace(0, length, updatedText)
                cursor = updatedCursor
            }
        }
        "⌫" -> {
            current_text_state.edit {
                val safeCursor = cursor.coerceIn(0, length)
                if (safeCursor > 0) {
                    delete(safeCursor - 1, safeCursor)
                    cursor = safeCursor - 1
                }
                if (length == 0) {
                    insert(0, "0")
                    cursor = 1
                }
            }
        }
        else -> {
            current_text_state.edit {
                var safeCursor = cursor.coerceIn(0, length)

                if (toString() == "0" && current_key_pressed != ".") {
                    replace(0, 1, "")
                    safeCursor = (safeCursor - 1).coerceAtLeast(0)
                }

                val toAppendBase = current_key_pressed
                val targetIndex = firstPlaceholderIndexFromCursor(toString(), safeCursor)

                if (targetIndex != null) {
                    val charAtTarget = toString()[targetIndex]
                    val replacement = when (charAtTarget) {
                        'ˣ', 'ʸ' -> toSuperscript(toAppendBase)
                        'ᵧ' -> toSubscript(toAppendBase)
                        else -> toAppendBase
                    }
                    replace(targetIndex, targetIndex + 1, replacement)
                    cursor = targetIndex + replacement.length
                } else {
                    val charBeforeCursor = toString().getOrNull((safeCursor - 1).coerceAtLeast(0))
                    val toAppend = when {
                        charBeforeCursor?.isSuperscript() == true && current_key_pressed.all { it.isDigit() } ->
                            toSuperscript(toAppendBase)
                        charBeforeCursor?.isSubscript() == true && current_key_pressed.all { it.isDigit() } ->
                            toSubscript(toAppendBase)
                        else -> toAppendBase
                    }
                    insert(safeCursor, toAppend)
                    cursor = safeCursor + toAppend.length
                }
            }
        }
    }

    cursorIndex = cursor.coerceIn(0, current_text_state.text.length)
}

private fun firstPlaceholderIndexFromCursor(text: String, cursorIndex: Int): Int? {
    val placeholders = charArrayOf('x', 'y', 'ˣ', 'ʸ', 'ᵧ')

    val afterCursor = placeholders
        .map { text.indexOf(it, startIndex = cursorIndex) }
        .filter { it >= 0 }
        .minOrNull()

    if (afterCursor != null) {
        return afterCursor
    }

    return placeholders
        .map { text.indexOf(it) }
        .filter { it >= 0 }
        .minOrNull()
}

private fun toSuperscript(input: String): String {
    val map = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        'x' to 'ˣ', 'y' to 'ʸ', '+' to '⁺', '-' to '⁻', '(' to '⁽', ')' to '⁾'
    )
    return input.map { map[it] ?: it }.joinToString("")
}

private fun toSubscript(input: String): String {
    val map = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        'x' to 'ₓ', 'y' to 'ᵧ', '+' to '₊', '-' to '₋', '(' to '₍', ')' to '₎'
    )
    return input.map { map[it] ?: it }.joinToString("")
}

private fun Char.isSuperscript(): Boolean = this in "⁰¹²³⁴⁵⁶⁷⁸⁹ˣʸ⁺⁻⁽⁾"
private fun Char.isSubscript(): Boolean = this in "₀₁₂₃₄₅₆₇₈₉ₓᵧ₊₋₍₎"

private fun plusMinusAtCursor(input: String, cursorIndex: Int): Pair<String, Int> {
    val end = cursorIndex.coerceIn(0, input.length)
    if (input.isEmpty() || end == 0) {
        return input to end
    }

    val lastPlus = input.lastIndexOf('+', startIndex = end - 1)
    val lastMinus = input.lastIndexOf('-', startIndex = end - 1)
    val lastSignIndex = maxOf(lastPlus, lastMinus)

    if (lastSignIndex >= 0) {
        val flipped = if (input[lastSignIndex] == '+') '-' else '+'
        val updated = buildString(input.length) {
            append(input, 0, lastSignIndex)
            append(flipped)
            append(input, lastSignIndex + 1, input.length)
        }
        return updated to end
    }

    var numberEnd = end - 1
    while (numberEnd >= 0 && !(input[numberEnd].isDigit() || input[numberEnd] == '.')) numberEnd--
    if (numberEnd < 0) return input to end

    var numberStart = numberEnd
    while (numberStart >= 0 && (input[numberStart].isDigit() || input[numberStart] == '.')) numberStart--
    numberStart++

    val updated = buildString(input.length + 1) {
        append(input, 0, numberStart)
        append('-')
        append(input, numberStart, input.length)
    }

    val cursorShift = if (end >= numberStart) 1 else 0
    return updated to (end + cursorShift).coerceAtMost(updated.length)
}