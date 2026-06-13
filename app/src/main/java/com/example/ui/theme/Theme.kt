package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldSecondary,
    onPrimary = CharcoalDark,
    primaryContainer = GoldPrimary,
    onPrimaryContainer = Color.White,
    secondary = SoftChalk,
    onSecondary = CharcoalDark,
    background = CharcoalDark,
    onBackground = WarmCream,
    surface = CharcoalMuted,
    onSurface = WarmCream,
    outline = MutedGrey,
    error = CrimsonAlert
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = LightGreyAccent,
    onPrimaryContainer = CharcoalDark,
    secondary = SoftChalk,
    onSecondary = CharcoalDark,
    background = WarmCream,
    onBackground = CharcoalDark,
    surface = PureWhite,
    onSurface = CharcoalDark,
    outline = SoftChalk,
    error = CrimsonAlert

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is premium, but for explicit Zara brand aesthetics we lock in our Royal Palette!
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
