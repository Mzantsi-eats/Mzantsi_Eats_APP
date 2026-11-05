package com.mzantsi.table.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The app colours (defined in Color.kt) and text sizes.
private val MzantsiColors = lightColorScheme(
    primary = MzantsiGreen,
    onPrimary = MzantsiCream,
    secondary = MzantsiOrange,
    onSecondary = MzantsiCream,
    tertiary = MzantsiGold,
    background = MzantsiCream,
    onBackground = MzantsiTextDark,
    surface = MzantsiCream,
    onSurface = MzantsiTextDark,
    error = MzantsiRed
)

private val MzantsiTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyMedium = TextStyle(fontSize = 15.sp),
    labelSmall = TextStyle(fontSize = 12.sp)
)

@Composable
fun MzantsiTableTheme(content: @Composable () -> Unit) {
    // We always use the light colours from our POE design (green stands for fertility and nature).
    // A dark theme can be added later using isSystemInDarkTheme().
    MaterialTheme(
        colorScheme = MzantsiColors,
        typography = MzantsiTypography,
        content = content
    )
}
