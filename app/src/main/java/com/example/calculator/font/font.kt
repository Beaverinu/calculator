package com.example.calculator.font

import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import com.example.calculator.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val Roboto_Mono = GoogleFont("Roboto Mono")

val Font_roboto_mono = FontFamily(
    Font(googleFont = Roboto_Mono, fontProvider = provider)
)
