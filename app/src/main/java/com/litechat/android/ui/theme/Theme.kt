package com.litechat.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark-first ChatGPT-like palette — easy on OLED + low-end screens
private val Bg = Color(0xFF0B0F14)
private val Surface = Color(0xFF141A22)
private val SurfaceAlt = Color(0xFF1C2430)
private val Accent = Color(0xFF6EE7B7)
private val OnAccent = Color(0xFF06281C)
private val Text = Color(0xFFE8EEF5)
private val Muted = Color(0xFF9AA8B5)
private val Danger = Color(0xFFFF8A80)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    secondary = Accent,
    onSecondary = OnAccent,
    background = Bg,
    onBackground = Text,
    surface = Surface,
    onSurface = Text,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = Muted,
    error = Danger,
    onError = Color.Black,
    outline = Color(0xFF2A3544),
)

@Composable
fun LiteChatTheme(content: @Composable () -> Unit) {
    // Always dark for consistent OLED + low RAM (no dynamic color engine)
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
