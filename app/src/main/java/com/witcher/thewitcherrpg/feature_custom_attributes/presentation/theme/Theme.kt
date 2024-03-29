package com.witcher.thewitcherrpg.feature_custom_attributes.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = primaryNight,
    primaryVariant = primaryVariantNight,
    secondary = secondaryNight,
    onPrimary = onPrimaryNight
)

private val LightColorPalette = lightColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    onPrimary = onPrimary

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun TheWitcherRPGTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}