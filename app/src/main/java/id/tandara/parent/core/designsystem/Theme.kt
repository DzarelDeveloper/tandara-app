package id.tandara.parent.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// =======================================================
// SEMANTIC DESIGN TOKENS FOR TANDARA PARENT APP
// =======================================================

data class TandaraColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val borderSubtle: Color,
    val divider: Color,
    val primary: Color,
    val primaryPressed: Color,
    val primarySubtle: Color,
    val accent: Color,
    val success: Color,
    val successSubtle: Color,
    val warning: Color,
    val warningSubtle: Color,
    val danger: Color,
    val dangerSubtle: Color,
    val info: Color,
    val infoSubtle: Color,
    val navigationBackground: Color,
    val navigationActive: Color,
    val navigationInactive: Color,
    val inputBackground: Color,
    val inputBorder: Color
)

// DEFAULT LIGHT THEME TOKENS
val LightTandaraColors = TandaraColors(
    isDark = false,
    background = Color(0xFFF4F7FB),       // Soft cool gray / blue-white
    surface = Color(0xFFFFFFFF),          // Pure white
    surfaceElevated = Color(0xFFFFFFFF),  // Elevated white
    surfaceSubtle = Color(0xFFF1F5F9),    // Cool subtle light surface for rows & chips
    textPrimary = Color(0xFF0F172A),      // Deep navy
    textSecondary = Color(0xFF475569),    // Blue-gray
    textMuted = Color(0xFF64748B),        // Soft muted text
    border = Color(0xFFE2E8F0),           // Soft gray-blue border
    borderSubtle = Color(0x332474FF),      // Subtle blue accent border
    divider = Color(0xFFE2E8F0),
    primary = Color(0xFF2474FF),          // Tandara Royal Blue
    primaryPressed = Color(0xFF1D5CE0),
    primarySubtle = Color(0xFFEFF6FF),    // Light blue tint
    accent = Color(0xFF2563EB),
    success = Color(0xFF16A34A),          // Green
    successSubtle = Color(0xFFDCFCE7),    // Light green
    warning = Color(0xFFD97706),          // Amber
    warningSubtle = Color(0xFFFEF3C7),    // Light amber
    danger = Color(0xFFDC2626),           // Red
    dangerSubtle = Color(0xFFFEE2E2),     // Light red
    info = Color(0xFF2563EB),             // Info blue
    infoSubtle = Color(0xFFDBEAFE),
    navigationBackground = Color(0xFFFFFFFF),
    navigationActive = Color(0xFF2474FF),
    navigationInactive = Color(0xFF64748B),
    inputBackground = Color(0xFFF8FAFC),
    inputBorder = Color(0xFFE2E8F0)
)

// DARK THEME TOKENS
val DarkTandaraColors = TandaraColors(
    isDark = true,
    background = Color(0xFF071426),       // Deepest navy background shell
    surface = Color(0xFF0D2038),          // Primary navy surface
    surfaceElevated = Color(0xFF163554),  // Elevated navy surface
    surfaceSubtle = Color(0xFF112A47),    // Secondary navy surface
    textPrimary = Color(0xFFF7FAFC),      // Crisp light text
    textSecondary = Color(0xFFA9B8CC),    // Soft secondary text
    textMuted = Color(0xFF71849D),        // Muted text
    border = Color(0xFF1E3858),           // Dark blue-gray border
    borderSubtle = Color(0x333B82F6),     // Subtle accent border
    divider = Color(0xFF152C48),
    primary = Color(0xFF2474FF),          // Tandara Royal Blue
    primaryPressed = Color(0xFF1D5CE0),
    primarySubtle = Color(0xFF132F52),    // Dark mode tint
    accent = Color(0xFF3B82F6),
    success = Color(0xFF22C77A),
    successSubtle = Color(0xFF0B2E1E),
    warning = Color(0xFFF5B942),
    warningSubtle = Color(0xFF382606),
    danger = Color(0xFFEF5B5B),
    dangerSubtle = Color(0xFF381414),
    info = Color(0xFF38BDF8),
    infoSubtle = Color(0xFF0E2E4A),
    navigationBackground = Color(0xFF0D2038),
    navigationActive = Color(0xFF2474FF),
    navigationInactive = Color(0xFF71849D),
    inputBackground = Color(0xFF112A47),
    inputBorder = Color(0xFF1E3858)
)

val LocalTandaraColors = staticCompositionLocalOf { LightTandaraColors }

object TandaraTheme {
    val colors: TandaraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTandaraColors.current
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2474FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF16A34A),
    onTertiary = Color.White,
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFFDC2626)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2474FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF112A47),
    onPrimaryContainer = Color(0xFFF7FAFC),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF163554),
    onSecondaryContainer = Color(0xFFF7FAFC),
    tertiary = Color(0xFF22C77A),
    onTertiary = Color.White,
    background = Color(0xFF071426),
    onBackground = Color(0xFFF7FAFC),
    surface = Color(0xFF0D2038),
    onSurface = Color(0xFFF7FAFC),
    surfaceVariant = Color(0xFF112A47),
    onSurfaceVariant = Color(0xFFA9B8CC),
    outline = Color(0xFF1E3858),
    outlineVariant = Color(0xFF152C48),
    error = Color(0xFFEF5B5B),
    onError = Color.White,
    errorContainer = Color(0xFF381414),
    onErrorContainer = Color(0xFFEF5B5B)
)

val TandaraShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),      // Status badges & small pills
    medium = RoundedCornerShape(12.dp),    // Buttons & Inputs
    large = RoundedCornerShape(14.dp),     // Cards & Surfaces
    extraLarge = RoundedCornerShape(16.dp) // Sheets & Dialogs
)

@Composable
fun TandaraTheme(
    darkTheme: Boolean = false, // DEFAULT IS LIGHT (false)
    content: @Composable () -> Unit
) {
    val tandaraColors = if (darkTheme) DarkTandaraColors else LightTandaraColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalTandaraColors provides tandaraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TandaraTypography,
            shapes = TandaraShapes,
            content = content
        )
    }
}
