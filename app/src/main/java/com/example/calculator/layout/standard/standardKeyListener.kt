package com.example.calculator.layout.standard

import com.example.calculator.layout.standard.current_keyboard_in_use
import com.example.calculator.layout.standard.current_text_in_standard_calculator


fun pressed_key(
    current_key_pressed: String
) {
    if(current_key_pressed == "2ⁿᵈ"){
        current_keyboard_in_use = 2
    }
    else if(current_key_pressed == "1ˢᵗ"){
        current_keyboard_in_use = 1
    }
}