package com.example.gestionfichiers.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionfichiers.R

/**
 * Identité visuelle « Cobalt net » (cahier § 4.8) : cobalt, menthe, neutres brume → nuit,
 * police Manrope. Les couleurs du téléphone (Android 12+) ne sont plus utilisées,
 * pour garder l'identité de l'application.
 */
object Cobalt {
    val C50 = Color(0xFFEEF2FF)
    val C100 = Color(0xFFDCE4FF)
    val C200 = Color(0xFFB9C9FF)
    val C300 = Color(0xFF8EA8FF)
    val C400 = Color(0xFF5B7FFF)
    val C500 = Color(0xFF2D5BFF)
    val C600 = Color(0xFF1F45E0)
    val C700 = Color(0xFF1835B0)
    val C800 = Color(0xFF142A85)
    val C900 = Color(0xFF0F1F5C)

    val Mint300 = Color(0xFF6FE0C4)
    val Mint500 = Color(0xFF18C29C)
    val Mint700 = Color(0xFF0E8A6E)

    val Warning = Color(0xFFFFB547)
    val Danger = Color(0xFFD93838)
    val DangerLight = Color(0xFFFF8A8A)

    val Mist = Color(0xFFF3F6FC)
    val Line = Color(0xFFDCE3F0)
    val Muted = Color(0xFF556079)
    val Slate = Color(0xFF9AA6C0)
    val Ink = Color(0xFF0F172A)
    val Night800 = Color(0xFF1A2340)
    val Night850 = Color(0xFF141A2C)
    val Night900 = Color(0xFF0E1322)
    val Night950 = Color(0xFF0B0F1A)
    val NightLine = Color(0xFF27304A)
    val Snow = Color(0xFFE6EBF5)
}

private val LightColors = lightColorScheme(
    primary = Cobalt.C500,
    onPrimary = Color.White,
    primaryContainer = Cobalt.C100,
    onPrimaryContainer = Cobalt.C900,
    inversePrimary = Cobalt.C300,
    secondary = Cobalt.Mint700,
    onSecondary = Color.White,
    secondaryContainer = Cobalt.C100,
    onSecondaryContainer = Cobalt.C800,
    tertiary = Cobalt.Mint500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5F6EC),
    onTertiaryContainer = Color(0xFF064E3F),
    background = Cobalt.Mist,
    onBackground = Cobalt.Ink,
    surface = Cobalt.Mist,
    onSurface = Cobalt.Ink,
    surfaceVariant = Color(0xFFE8EDF7),
    onSurfaceVariant = Cobalt.Muted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFE),
    surfaceContainer = Color(0xFFE8EDF7),
    surfaceContainerHigh = Color(0xFFE1E7F3),
    surfaceContainerHighest = Cobalt.Line,
    inverseSurface = Cobalt.Ink,
    inverseOnSurface = Cobalt.Snow,
    outline = Cobalt.Slate,
    outlineVariant = Cobalt.Line,
    error = Cobalt.Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFDE4E4),
    onErrorContainer = Color(0xFFB42323)
)

private val DarkColors = darkColorScheme(
    primary = Cobalt.C300,
    onPrimary = Cobalt.C900,
    primaryContainer = Color(0xFF22305E),
    onPrimaryContainer = Cobalt.C100,
    inversePrimary = Cobalt.C500,
    secondary = Cobalt.Mint300,
    onSecondary = Color(0xFF053B30),
    secondaryContainer = Color(0xFF22305E),
    onSecondaryContainer = Cobalt.C100,
    tertiary = Cobalt.Mint300,
    onTertiary = Color(0xFF053B30),
    tertiaryContainer = Color(0xFF0B4A3C),
    onTertiaryContainer = Color(0xFFBDF3E3),
    background = Cobalt.Night950,
    onBackground = Cobalt.Snow,
    surface = Cobalt.Night900,
    onSurface = Cobalt.Snow,
    surfaceVariant = Cobalt.Night800,
    onSurfaceVariant = Cobalt.Slate,
    // Lignes de la liste : un cran au-dessus du fond
    surfaceContainerLowest = Cobalt.Night850,
    surfaceContainerLow = Color(0xFF111729),
    surfaceContainer = Cobalt.Night850,
    surfaceContainerHigh = Cobalt.Night800,
    surfaceContainerHighest = Cobalt.NightLine,
    inverseSurface = Cobalt.Snow,
    inverseOnSurface = Cobalt.Ink,
    outline = Color(0xFF5A6681),
    outlineVariant = Cobalt.NightLine,
    error = Cobalt.DangerLight,
    onError = Color(0xFF3A1A22),
    errorContainer = Color(0xFF3A1A22),
    onErrorContainer = Cobalt.DangerLight
)

val Manrope = FontFamily(
    Font(R.font.manrope_400, FontWeight.Normal),
    Font(R.font.manrope_500, FontWeight.Medium),
    Font(R.font.manrope_600, FontWeight.SemiBold),
    Font(R.font.manrope_700, FontWeight.Bold),
    Font(R.font.manrope_800, FontWeight.ExtraBold)
)

/** Échelle de la planche : titre 26/800, section 18/700, corps 15/500, détail 12/500, étiquette 11/700. */
private val AppTypography: Typography = Typography().let { base ->
    fun TextStyle.m(weight: FontWeight? = null) =
        copy(fontFamily = Manrope, fontWeight = weight ?: fontWeight)
    Typography(
        displayLarge = base.displayLarge.m(FontWeight.ExtraBold),
        displayMedium = base.displayMedium.m(FontWeight.ExtraBold),
        displaySmall = base.displaySmall.m(FontWeight.ExtraBold),
        headlineLarge = base.headlineLarge.m(FontWeight.ExtraBold),
        headlineMedium = base.headlineMedium.m(FontWeight.ExtraBold),
        headlineSmall = base.headlineSmall.m(FontWeight.ExtraBold),
        titleLarge = base.titleLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.2).sp),
        titleMedium = base.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 18.sp),
        titleSmall = base.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
        bodyLarge = base.bodyLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 15.sp),
        bodyMedium = base.bodyMedium.m(FontWeight.Medium),
        bodySmall = base.bodySmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 12.sp),
        labelLarge = base.labelLarge.m(FontWeight.Bold),
        labelMedium = base.labelMedium.m(FontWeight.Bold),
        labelSmall = base.labelSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun GestionFichiersTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

/** Vrai en thème sombre (pour les couleurs par type de fichier et les barres). */
@Composable
fun isDark(): Boolean = isSystemInDarkTheme()
