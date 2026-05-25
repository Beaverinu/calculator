package com.example.calculator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPurple,
    secondary = LightPurplePink,
    tertiary = Pink80,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DarkPurple,
    secondary = LightPurplePink,
    tertiary = Pink40,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val duration = 250
    val colorScheme = targetColorScheme.copy(
        primary = animateColorAsState(targetColorScheme.primary, tween(duration), label = "primary").value,
        onPrimary = animateColorAsState(targetColorScheme.onPrimary, tween(duration), label = "onPrimary").value,
        primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, tween(duration), label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, tween(duration), label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, tween(duration), label = "inversePrimary").value,
        secondary = animateColorAsState(targetColorScheme.secondary, tween(duration), label = "secondary").value,
        onSecondary = animateColorAsState(targetColorScheme.onSecondary, tween(duration), label = "onSecondary").value,
        secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, tween(duration), label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, tween(duration), label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(targetColorScheme.tertiary, tween(duration), label = "tertiary").value,
        onTertiary = animateColorAsState(targetColorScheme.onTertiary, tween(duration), label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, tween(duration), label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, tween(duration), label = "onTertiaryContainer").value,
        background = animateColorAsState(targetColorScheme.background, tween(duration), label = "background").value,
        onBackground = animateColorAsState(targetColorScheme.onBackground, tween(duration), label = "onBackground").value,
        surface = animateColorAsState(targetColorScheme.surface, tween(duration), label = "surface").value,
        onSurface = animateColorAsState(targetColorScheme.onSurface, tween(duration), label = "onSurface").value,
        surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, tween(duration), label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, tween(duration), label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(targetColorScheme.surfaceTint, tween(duration), label = "surfaceTint").value,
        inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, tween(duration), label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, tween(duration), label = "inverseOnSurface").value,
        error = animateColorAsState(targetColorScheme.error, tween(duration), label = "error").value,
        onError = animateColorAsState(targetColorScheme.onError, tween(duration), label = "onError").value,
        errorContainer = animateColorAsState(targetColorScheme.errorContainer, tween(duration), label = "errorContainer").value,
        onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, tween(duration), label = "onErrorContainer").value,
        outline = animateColorAsState(targetColorScheme.outline, tween(duration), label = "outline").value,
        outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, tween(duration), label = "outlineVariant").value,
        scrim = animateColorAsState(targetColorScheme.scrim, tween(duration), label = "scrim").value,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}