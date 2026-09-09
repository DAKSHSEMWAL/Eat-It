package com.daksh.eatit.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Primitive values stay here; consumers use semantic Material roles and EatItTheme tokens. */
object EatItPalette {
    val Forest = Color(0xFF24543D)
    val Citrus = Color(0xFFDCEB83)
    val Ink = Color(0xFF19231C)
    val Cream = Color(0xFFFAF9F3)
    val Clay = Color(0xFF9D432B)
}
@Immutable
data class EatItSpacing(
    val xxs: androidx.compose.ui.unit.Dp = 4.dp,
    val xs: androidx.compose.ui.unit.Dp = 8.dp,
    val sm: androidx.compose.ui.unit.Dp = 12.dp,
    val md: androidx.compose.ui.unit.Dp = 16.dp,
    val lg: androidx.compose.ui.unit.Dp = 24.dp,
    val xl: androidx.compose.ui.unit.Dp = 32.dp,
    val xxl: androidx.compose.ui.unit.Dp = 48.dp,
)
@Immutable
data class EatItSizing(
    val touchTarget: androidx.compose.ui.unit.Dp = 48.dp,
    val button: androidx.compose.ui.unit.Dp = 56.dp,
    val readingWidth: androidx.compose.ui.unit.Dp = 720.dp,
    val foodImage: androidx.compose.ui.unit.Dp = 168.dp,
)
object EatItMotion { const val Fast = 150; const val Standard = 250; const val Emphasis = 400 }
object EatItElevation { val None = 0.dp; val Card = 1.dp; val Raised = 3.dp; val Overlay = 6.dp }
private val LocalSpacing = staticCompositionLocalOf { EatItSpacing() }
private val LocalSizing = staticCompositionLocalOf { EatItSizing() }
object EatItTheme {
    val spacing: EatItSpacing @Composable get() = LocalSpacing.current
    val sizing: EatItSizing @Composable get() = LocalSizing.current
}
private val Light = lightColorScheme(
    primary = EatItPalette.Forest, onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EAD9), onPrimaryContainer = Color(0xFF153C29),
    secondary = Color(0xFF536027), onSecondary = Color.White,
    secondaryContainer = EatItPalette.Citrus, onSecondaryContainer = Color(0xFF252D0C),
    tertiary = EatItPalette.Clay, onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCD), onTertiaryContainer = Color(0xFF3C1004),
    background = EatItPalette.Cream, onBackground = EatItPalette.Ink,
    surface = EatItPalette.Cream, onSurface = EatItPalette.Ink,
    surfaceVariant = Color(0xFFE4E7DD), onSurfaceVariant = Color(0xFF454C43),
    surfaceContainer = Color(0xFFEEEFE7), surfaceContainerLow = Color(0xFFF4F4EC),
    surfaceContainerHigh = Color(0xFFE8EAE1), surfaceContainerHighest = Color(0xFFE2E4DA),
    surfaceContainerLowest = Color.White,
    outline = Color(0xFF747C70), outlineVariant = Color(0xFFC4CBBE),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)
private val Dark = darkColorScheme(
    primary = Color(0xFFA5D2AE), onPrimary = Color(0xFF103822),
    primaryContainer = Color(0xFF2D5039), onPrimaryContainer = Color(0xFFC1EEC9),
    secondary = EatItPalette.Citrus, onSecondary = Color(0xFF29320C),
    secondaryContainer = Color(0xFF404B1C), onSecondaryContainer = Color(0xFFE0EDA4),
    tertiary = Color(0xFFFFB59A), onTertiary = Color(0xFF5E200D),
    tertiaryContainer = Color(0xFF7C3520), onTertiaryContainer = Color(0xFFFFDBCD),
    background = Color(0xFF111611), onBackground = Color(0xFFE1E5DB),
    surface = Color(0xFF111611), onSurface = Color(0xFFE1E5DB),
    surfaceVariant = Color(0xFF42493F), onSurfaceVariant = Color(0xFFC2C9BC),
    surfaceContainerLowest = Color(0xFF0C110C), surfaceContainerLow = Color(0xFF191E18),
    surfaceContainer = Color(0xFF1D221C), surfaceContainerHigh = Color(0xFF272C26),
    surfaceContainerHighest = Color(0xFF323730),
    outline = Color(0xFF8C9587), outlineVariant = Color(0xFF42493F),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)
private fun type(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.SansSerif, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight,
)
val EatItTypography = Typography(
    displayLarge = type(57, 64, FontWeight.Bold), displayMedium = type(45, 52, FontWeight.Bold),
    displaySmall = type(36, 44, FontWeight.Bold), headlineLarge = type(32, 40, FontWeight.Bold),
    headlineMedium = type(28, 36, FontWeight.Bold), headlineSmall = type(24, 32, FontWeight.SemiBold),
    titleLarge = type(22, 28, FontWeight.SemiBold), titleMedium = type(16, 24, FontWeight.SemiBold),
    titleSmall = type(14, 20, FontWeight.SemiBold), bodyLarge = type(16, 24), bodyMedium = type(14, 20),
    bodySmall = type(12, 16), labelLarge = type(14, 20, FontWeight.SemiBold),
    labelMedium = type(12, 16, FontWeight.SemiBold), labelSmall = type(11, 16, FontWeight.SemiBold),
)
@Composable
fun EatItTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides EatItSpacing(), LocalSizing provides EatItSizing()) {
        MaterialTheme(
            colorScheme = if (darkTheme) Dark else Light,
            typography = EatItTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(4.dp), small = RoundedCornerShape(8.dp),
                medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(32.dp),
            ), content = content,
        )
    }
}
