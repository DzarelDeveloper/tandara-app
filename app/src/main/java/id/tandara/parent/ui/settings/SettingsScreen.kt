package id.tandara.parent.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.BuildConfig
import id.tandara.parent.core.designsystem.TandaraTheme
import id.tandara.parent.ui.components.ConfirmationDialog
import id.tandara.parent.ui.components.TandaraBrandTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = TandaraTheme.colors

    var notifPresensiEnabled by remember { mutableStateOf(true) }
    var notifIzinEnabled by remember { mutableStateOf(true) }
    var showPasswordNoticeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutSuccess()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    // Theme Selector Bottom Sheet
    if (uiState.showThemeDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissThemeDialog() },
            sheetState = sheetState,
            containerColor = colors.surface,
            contentColor = colors.textPrimary,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 12.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
                    .testTag("theme_selection_sheet")
            ) {
                Text(
                    text = "Pilih Tema",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThemeOptionItem(
                    title = "Terang",
                    description = "Tampilan cerah, default Tandara",
                    icon = Icons.Outlined.LightMode,
                    isSelected = uiState.appearance == "light",
                    onClick = { viewModel.selectTheme("light") },
                    testTag = "theme_option_light"
                )

                HorizontalDivider(color = colors.divider)

                ThemeOptionItem(
                    title = "Gelap",
                    description = "Tampilan dark navy, nyaman untuk mata",
                    icon = Icons.Outlined.DarkMode,
                    isSelected = uiState.appearance == "dark",
                    onClick = { viewModel.selectTheme("dark") },
                    testTag = "theme_option_dark"
                )

                HorizontalDivider(color = colors.divider)

                ThemeOptionItem(
                    title = "Ikuti Sistem",
                    description = "Menyesuaikan dengan pengaturan perangkat",
                    icon = Icons.Outlined.PhoneAndroid,
                    isSelected = uiState.appearance == "system",
                    onClick = { viewModel.selectTheme("system") },
                    testTag = "theme_option_system"
                )
            }
        }
    }

    // Password notice dialog (since parent account password is school-managed)
    if (showPasswordNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordNoticeDialog = false },
            title = {
                Text(
                    text = "Perubahan Kata Sandi",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Akun wali murid dikelola secara terpusat oleh sekolah. Untuk meminta perubahan atau reset kata sandi, silakan hubungi Admin IT atau Tata Usaha SMK Taman Harapan.",
                    color = colors.textSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPasswordNoticeDialog = false }) {
                    Text("Mengerti", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Logout confirmation dialog
    if (uiState.showLogoutConfirmDialog) {
        ConfirmationDialog(
            title = "Keluar dari akun?",
            message = "Anda akan diarahkan kembali ke halaman masuk.",
            confirmLabel = "Keluar",
            isDestructive = true,
            onConfirm = { viewModel.confirmLogout() },
            onDismiss = { viewModel.dismissLogoutConfirmDialog() },
            testTag = "logout_confirm_dialog"
        )
    }

    Scaffold(
        topBar = {
            TandaraBrandTopAppBar(
                onNotificationClick = {},
                tagline = "Pengaturan Akun & Aplikasi",
                hasUnreadNotifications = false
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 36.dp)
                .testTag("settings_screen_content")
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Pengaturan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // PROFILE HEADER CARD (Tap opens Parent Profile)
            // ========================================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .clickable { onNavigateToProfile() }
                    .testTag("settings_profile_card"),
                color = colors.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(colors.surfaceSubtle, CircleShape)
                            .border(1.5.dp, colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.currentParent?.name ?: "Memuat profil...",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Orang Tua / Wali",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.maskedPhoneNumber,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = "Lihat Profil",
                        tint = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========================================================
            // GROUP 1: AKUN
            // ========================================================
            SettingsGroupHeader(title = "AKUN")
            SettingsGroupSurface {
                SettingsNavigationRow(
                    icon = Icons.Outlined.Person,
                    label = "Profil Saya",
                    subtitle = "Informasi akun orang tua/wali",
                    onClick = onNavigateToProfile,
                    testTag = "settings_row_profil"
                )
                HorizontalDivider(color = colors.divider)
                SettingsNavigationRow(
                    icon = Icons.Outlined.Lock,
                    label = "Ubah Kata Sandi",
                    subtitle = "Dikelola oleh pihak sekolah",
                    onClick = { showPasswordNoticeDialog = true },
                    testTag = "settings_row_change_password"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // GROUP 2: TAMPILAN
            // ========================================================
            SettingsGroupHeader(title = "TAMPILAN")
            SettingsGroupSurface {
                val themeLabel = when (uiState.appearance) {
                    "dark" -> "Gelap"
                    "system" -> "Ikuti Sistem"
                    else -> "Terang"
                }
                SettingsNavigationRowWithTrailing(
                    icon = if (colors.isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    label = "Tema",
                    subtitle = "Pengaturan tema aplikasi",
                    trailingText = themeLabel,
                    onClick = { viewModel.openThemeDialog() },
                    testTag = "settings_row_theme"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // GROUP 3: NOTIFIKASI
            // ========================================================
            SettingsGroupHeader(title = "NOTIFIKASI")
            SettingsGroupSurface {
                SettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    label = "Notifikasi Presensi",
                    subtitle = "Pemberitahuan saat anak masuk & pulang",
                    checked = notifPresensiEnabled,
                    onCheckedChange = { notifPresensiEnabled = it },
                    testTag = "settings_toggle_presensi"
                )
                HorizontalDivider(color = colors.divider)
                SettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    label = "Notifikasi Izin",
                    subtitle = "Pembaruan status verifikasi izin sekolah",
                    checked = notifIzinEnabled,
                    onCheckedChange = { notifIzinEnabled = it },
                    testTag = "settings_toggle_izin"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // GROUP 4: PRIVASI & KEAMANAN
            // ========================================================
            SettingsGroupHeader(title = "PRIVASI & KEAMANAN")
            SettingsGroupSurface {
                SettingsNavigationRow(
                    icon = Icons.Outlined.Security,
                    label = "Privasi & Data",
                    subtitle = "Kebijakan data pengenalan wajah siswa",
                    onClick = onNavigateToPrivacy,
                    testTag = "settings_row_privasi"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // GROUP 5: BANTUAN
            // ========================================================
            SettingsGroupHeader(title = "BANTUAN")
            SettingsGroupSurface {
                SettingsNavigationRow(
                    icon = Icons.Outlined.HelpOutline,
                    label = "Pusat Bantuan",
                    subtitle = "Pertanyaan umum & tata cara presensi",
                    onClick = onNavigateToFaq,
                    testTag = "settings_row_bantuan"
                )
                HorizontalDivider(color = colors.divider)
                SettingsNavigationRow(
                    icon = Icons.Outlined.Info,
                    label = "Tentang Tandara",
                    subtitle = "Versi ${BuildConfig.VERSION_NAME} • SMK Taman Harapan",
                    onClick = onNavigateToAbout,
                    testTag = "settings_row_tentang"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ========================================================
            // GROUP 6: KELUAR (Destructive Action)
            // ========================================================
            SettingsGroupSurface {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openLogoutConfirmDialog() }
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .testTag("settings_row_keluar"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(colors.dangerSubtle, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Keluar",
                            tint = colors.danger,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keluar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.danger
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Keluar dari sesi akun orang tua",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Version Footer
            Text(
                text = "Tandara Parent App • SMK Taman Harapan",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textMuted
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = TandaraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.primarySubtle else colors.surfaceSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.primary else colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.primary else colors.textPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary
                )
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.primary,
                unselectedColor = colors.border
            )
        )
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    val colors = TandaraTheme.colors
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = colors.textMuted,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroupSurface(content: @Composable () -> Unit) {
    val colors = TandaraTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
        color = colors.surface,
        shadowElevation = 1.dp
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = TandaraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(colors.surfaceSubtle, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted
        )
    }
}

@Composable
private fun SettingsNavigationRowWithTrailing(
    icon: ImageVector,
    label: String,
    subtitle: String,
    trailingText: String,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = TandaraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(colors.surfaceSubtle, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.surfaceSubtle,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val colors = TandaraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(colors.surfaceSubtle, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceSubtle,
                uncheckedBorderColor = colors.border
            )
        )
    }
}
