package id.tandara.parent.ui.auth

data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val isSuccess: Boolean = false
)
