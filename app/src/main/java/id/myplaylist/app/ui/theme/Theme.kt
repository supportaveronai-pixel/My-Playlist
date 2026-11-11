package id.myplaylist.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    val BgWhite = Color(0xFFFFFFFF)
    val SurfaceLight = Color(0xFFF6F5FA)
    val SurfaceAlt = Color(0xFFEDE9FE)
    val BorderLight = Color(0xFFE5E7EB)
    val BorderInput = Color(0xFF71717A)
    val TextPrimary = Color(0xFF111111)
    val TextMuted = Color(0xFF52525B)
    val AccentViolet = Color(0xFF6D28D9)
    val AccentPressed = Color(0xFF5B21B6)
    val OnAccent = Color(0xFFFFFFFF)
    val DangerRed = Color(0xFFDC2626)
}

object AppSpace {
    val s1 = 8.dp
    val s2 = 16.dp
    val s3 = 24.dp
    val s4 = 32.dp
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.AccentViolet,
    onPrimary = AppColors.OnAccent,
    primaryContainer = AppColors.SurfaceAlt,
    onPrimaryContainer = AppColors.AccentViolet,
    secondary = AppColors.AccentPressed,
    onSecondary = AppColors.OnAccent,
    secondaryContainer = AppColors.SurfaceLight,
    onSecondaryContainer = AppColors.TextPrimary,
    background = AppColors.BgWhite,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.BgWhite,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceLight,
    onSurfaceVariant = AppColors.TextMuted,
    outline = AppColors.BorderInput,
    outlineVariant = AppColors.BorderLight,
    error = AppColors.DangerRed,
    onError = AppColors.OnAccent
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold,
        color = AppColors.TextPrimary
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.TextMuted
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.TextMuted
    )
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MyPlaylistTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
