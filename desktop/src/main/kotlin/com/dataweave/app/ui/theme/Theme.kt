package com.dataweave.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.dataweave.app.preferences.ThemeMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF1565C0),
        onPrimary = Color.White,
        secondary = Color(0xFF00897B),
        background = Color(0xFFF4F6F8),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE8EDF2),
        outline = Color(0xFFB0BEC5),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF90CAF9),
        onPrimary = Color(0xFF0D2740),
        secondary = Color(0xFF80CBC4),
        background = Color(0xFF121820),
        surface = Color(0xFF1A2330),
        surfaceVariant = Color(0xFF243040),
        outline = Color(0xFF546E7A),
    )

private val DwTypography =
    Typography(
        titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
        titleSmall = Typography().titleSmall.copy(fontWeight = FontWeight.Medium),
        bodySmall = Typography().bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
        labelSmall = Typography().labelSmall.copy(letterSpacing = 0.1.sp),
    )

@Composable
fun DataWeaveTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val useDark =
        when (themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> darkTheme
        }
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        typography = DwTypography,
        content = content,
    )
}
