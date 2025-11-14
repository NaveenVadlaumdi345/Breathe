package uk.ac.tees.mad.breathe.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---- Color palette (predefined, use across the app) ----
// Primary palette (calming blues/teals)
val BreathePrimary = Color(0xFF2D9CDB)       // main brand
val BreathePrimaryVariant = Color(0xFF1B7FB0)
val BreatheOnPrimary = Color(0xFFFFFFFF)

// Secondary palette (soft accent)
val BreatheSecondary = Color(0xFF7BD389)
val BreatheOnSecondary = Color(0xFF00331A)

// Surface & Background
val BreatheSurface = Color(0xFFF7FBFD)
val BreatheBackground = Color(0xFFEFF8FF)
val BreatheOnSurface = Color(0xFF0B3B4A)

// Muted / neutral
val BreatheMuted = Color(0xFF95A5A6)
val BreatheBorder = Color(0xFFDAEAF1)

// Error
val BreatheError = Color(0xFFEF5350)
val BreatheOnError = Color(0xFFFFFFFF)

// ---- Light & Dark color schemes (Material3) ----
private val LightColors = lightColorScheme(
    primary = BreathePrimary,
    onPrimary = BreatheOnPrimary,
    secondary = BreatheSecondary,
    onSecondary = BreatheOnSecondary,
    surface = BreatheSurface,
    background = BreatheBackground,
    onBackground = BreatheOnSurface,
    onSurface = BreatheOnSurface,
    error = BreatheError,
    onError = BreatheOnError
)

private val DarkColors = darkColorScheme(
    primary = BreathePrimary,
    onPrimary = Color.Black,
    secondary = BreatheSecondary,
    onSecondary = Color.Black,
    surface = Color(0xFF071419),
    background = Color(0xFF001218),
    onBackground = Color(0xFFBEEAF2),
    onSurface = Color(0xFFBEEAF2),
    error = BreatheError,
    onError = Color.Black
)

@Composable
fun BreatheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(), // use default M3 or customize
        content = content
    )
}

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

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

//@Composable
//fun BreatheTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}