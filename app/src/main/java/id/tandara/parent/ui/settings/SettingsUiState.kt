package id.tandara.parent.ui.settings

import id.tandara.parent.domain.model.Parent

data class SettingsUiState(
    val currentParent: Parent? = null,
    val username: String = "",
    val maskedPhoneNumber: String = "",
    val appearance: String = "light", // "light", "dark", "system"
    val showThemeDialog: Boolean = false,
    val isNotificationsEnabled: Boolean = false,
    val showNotificationInfoDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val showFaqDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val isLoggedOut: Boolean = false
)
