package com.nicolaielgame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameColorScheme = darkColorScheme(
    primary = Mint,
    secondary = Sun,
    tertiary = Signal,
    background = Ink,
    surface = Panel,
    onPrimary = Color(0xFF041411),
    onSecondary = Color(0xFF231A05),
    onTertiary = Color.White,
    onBackground = Color(0xFFEAF7F2),
    onSurface = Color(0xFFEAF7F2),
)

@Composable
fun DenicolaielTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GameColorScheme,
        content = content,
    )
}

