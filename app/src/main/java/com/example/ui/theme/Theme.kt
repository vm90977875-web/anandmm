package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BeatifyDarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    primaryContainer = SpotifyGreenDark,
    onPrimaryContainer = Color.White,
    secondary = SpotifyGreenLight,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF282E38)
)

private val PureBlackColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = Color(0xFF101010),
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF1C1C1C),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = SpotifyGreenDark,
    onPrimary = Color.White,
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF191414),
    surface = Color.White,
    onSurface = Color(0xFF191414),
    surfaceVariant = Color(0xFFEBEBEB),
    onSurfaceVariant = Color(0xFF535353)
)

@Composable
fun BeatifyTheme(
    pureBlack: Boolean = false,
    lightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        lightMode -> LightColorScheme
        pureBlack -> PureBlackColorScheme
        else -> BeatifyDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
