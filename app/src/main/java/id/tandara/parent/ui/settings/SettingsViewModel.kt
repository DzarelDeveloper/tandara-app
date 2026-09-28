package id.tandara.parent.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.tandara.parent.core.common.PhoneUtils
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.session.SessionManager
import id.tandara.parent.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeParentSession()
        observePreferences()
    }

    private fun observeParentSession() {
        viewModelScope.launch {
            authRepository.currentParentSession.collect { parent ->
                val masked = if (parent != null) {
                    PhoneUtils.maskPhoneNumber(parent.phoneNumber)
                } else {
                    ""
                }
                _uiState.update {
                    it.copy(
                        currentParent = parent,
                        maskedPhoneNumber = masked
                    )
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            sessionManager.sessionFlow.collect { session ->
                _uiState.update { it.copy(username = session.username) }
            }
        }
        viewModelScope.launch {
            sessionManager.notificationsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(isNotificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            sessionManager.appearanceFlow.collect { appearance ->
                _uiState.update { it.copy(appearance = appearance) }
            }
        }
    }

    fun openThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = true) }
    }

    fun dismissThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = false) }
    }

    fun selectTheme(appearance: String) {
        viewModelScope.launch {
            sessionManager.setAppearance(appearance)
            _uiState.update { it.copy(appearance = appearance, showThemeDialog = false) }
        }
    }

    fun onToggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setNotificationsEnabled(enabled)
            if (enabled) {
                _uiState.update { it.copy(showNotificationInfoDialog = true) }
            }
        }
    }

    fun dismissNotificationInfoDialog() {
        _uiState.update { it.copy(showNotificationInfoDialog = false) }
    }

    fun openChangePasswordDialog() {
        _uiState.update { it.copy(showChangePasswordDialog = true) }
    }

    fun dismissChangePasswordDialog() {
        _uiState.update { it.copy(showChangePasswordDialog = false) }
    }

    fun submitChangePassword(currentPass: String, newPass: String, confirmPass: String) {
        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Semua kolom password wajib diisi.") }
            return
        }
        if (newPass.length < 8) {
            _uiState.update { it.copy(snackbarMessage = "Password baru minimal 8 karakter.") }
            return
        }
        if (newPass != confirmPass) {
            _uiState.update { it.copy(snackbarMessage = "Konfirmasi password baru tidak cocok.") }
            return
        }

        viewModelScope.launch {
            val result = authRepository.changePassword(currentPass, newPass)
            _uiState.update {
                it.copy(
                    showChangePasswordDialog = false,
                    snackbarMessage = (result as? ApiResult.BackendUnavailable)?.message
                        ?: "Perubahan password memerlukan backend sekolah."
                )
            }
        }
    }

    fun openPrivacyDialog() {
        _uiState.update { it.copy(showPrivacyDialog = true) }
    }

    fun dismissPrivacyDialog() {
        _uiState.update { it.copy(showPrivacyDialog = false) }
    }

    fun openFaqDialog() {
        _uiState.update { it.copy(showFaqDialog = true) }
    }

    fun dismissFaqDialog() {
        _uiState.update { it.copy(showFaqDialog = false) }
    }

    fun openAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun dismissAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    fun openLogoutConfirmDialog() {
        _uiState.update { it.copy(showLogoutConfirmDialog = true) }
    }

    fun dismissLogoutConfirmDialog() {
        _uiState.update { it.copy(showLogoutConfirmDialog = false) }
    }

    fun confirmLogout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(showLogoutConfirmDialog = false, isLoggedOut = true) }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(authRepository, sessionManager) as T
        }
    }
}
