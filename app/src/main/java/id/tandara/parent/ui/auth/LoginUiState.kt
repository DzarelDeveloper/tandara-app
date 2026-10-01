package id.tandara.parent.ui.auth

import id.tandara.parent.core.network.ServerConfig

enum class ConnectionTestState {
    IDLE,
    TESTING,
    SUCCESS,
    FAILURE
}

data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val isSuccess: Boolean = false,
    val showServerSettingsDialog: Boolean = false,
    val serverHostInput: String = "",
    val serverPortInput: String = ServerConfig.DEFAULT_PORT.toString(),
    val serverConnectionTestState: ConnectionTestState = ConnectionTestState.IDLE,
    val serverConnectionTestMessage: String? = null,
    val serverChangeNoticeMessage: String? = null,
    val serverHostError: String? = null,
    val serverPortError: String? = null,
    val serverSaveLoading: Boolean = false
)
