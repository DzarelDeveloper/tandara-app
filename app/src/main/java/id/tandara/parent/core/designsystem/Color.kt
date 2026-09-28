package id.tandara.parent.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// =======================================================
// DYNAMIC THEME-REACTIVE COLOR TOKENS
// =======================================================

// Base Surfaces & Backgrounds
val AppBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.background

val PrimarySurface: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.surface

val SecondarySurface: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.surfaceSubtle

val ElevatedSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.surfaceElevated

val DarkNavyHeader: Color
    @Composable
    @ReadOnlyComposable
    get() = if (TandaraTheme.colors.isDark) Color(0xFF0A1C33) else Color(0xFFFFFFFF)

// Brand Blues
val PrimaryBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.primary

val PrimaryBluePressed: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.primaryPressed

val AccentBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.accent

val PrimaryBlueLight: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.primarySubtle

// Typography Colors
val PrimaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.textPrimary

val SecondaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.textSecondary

val MutedText: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.textMuted

// Borders & Dividers
val BorderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.border

val BorderColorLight: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.borderSubtle

val DividerColor: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.divider

// Semantic Status Colors
val SuccessGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.success

val SuccessGreenBg: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.successSubtle

val WarningAmber: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.warning

val WarningAmberBg: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.warningSubtle

val ErrorRed: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.danger

val ErrorRedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.dangerSubtle

val InfoBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.info

val InfoBlueBg: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.infoSubtle

// High-Contrast / Compatibility Helpers
val SurfaceWhite = Color(0xFFFFFFFF)
val HighContrastText = Color(0xFF0F172A)
val HighContrastSubtext = Color(0xFF475569)

val DeepNavy: Color
    @Composable
    @ReadOnlyComposable
    get() = TandaraTheme.colors.textPrimary

val TealAccent = Color(0xFF14B8A6)
val TealAccentLight = Color(0xFF0D3330)

val SuccessGreenLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SuccessGreenBg

val WarningAmberLight: Color
    @Composable
    @ReadOnlyComposable
    get() = WarningAmberBg

val ErrorRedLight: Color
    @Composable
    @ReadOnlyComposable
    get() = ErrorRedBg

val MutedSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = SecondarySurface
