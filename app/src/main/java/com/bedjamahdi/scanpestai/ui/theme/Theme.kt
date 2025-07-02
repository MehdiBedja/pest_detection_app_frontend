package com.bedjamahdi.scanpestai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// === Custom Color Palette ===

// Light Theme Colors
private val LightPrimary = Color(0xFF503D23) // Accent green
private val LightBackground = Color(0xFFF6F4E8) // Soft white/beige
private val LightSurface = Color(0xFFFFFFFF) // Card background
private val LightOnPrimary = Color.White
private val LightOnBackground = Color(0xFF503D23) // MarronColor for text
private val LightOnSurface = Color(0xFF000000) // black
private val LightSecondary = Color(0xFF503D23)

// Dark Theme Colors (inferred for good contrast and design harmony)
private val DarkPrimary = Color(0xFFB5C89A) // Softer green for dark mode
private val DarkBackground = Color(0xFF1A1A1A) // Near black for contrast
private val DarkSurface = Color(0xFF2A2A2A) // Slightly lighter than background
private val DarkOnPrimary = Color.Black
private val DarkOnBackground = Color(0xFFF6F4E8) // Light text on dark
private val DarkOnSurface = Color(0xFFCCCCCC) // Light gray for secondary text
private val DarkSecondary = Color(0xFF000000)

// === Light Theme Color Scheme ===
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface ,
    secondaryContainer = Color.Black ,
    secondary = LightSecondary
)

// === Dark Theme Color Scheme ===
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface ,
    secondaryContainer = Color.White,
    secondary = DarkSecondary
)

// === Theme Composable ===


@Composable
fun Pest_Detection_AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // ✅ Use your custom typography here
        content = content
    )
}


// Define color palette
val WhiteBackground = Color(0xFFF6F4E8)
val MarronColor =  Color(0xFF503D23)
val GrayText = Color(0xFF8A8A8A)
val AccentGreen = Color(0xFF9CB36B)
val CardBackground = Color(0xFFF3F3E5)


