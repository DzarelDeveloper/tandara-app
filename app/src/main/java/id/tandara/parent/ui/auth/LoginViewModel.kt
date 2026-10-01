package id.tandara.parent.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.core.network.ConfigUpdateResult
import id.tandara.parent.core.network.NetworkConfigManager
import id.tandara.parent.core.network.ServerConfig
import id.tandara.parent.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val networkConfigManager: NetworkConfigManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = networkConfigManager.activeConfig.first()
            _uiState.update {
                it.copy(
                    serverHostInput = current.host,
                    serverPortInput = current.port.toString()
                )
            }
        }
    }

    fun onPhoneNumberChanged(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun showForgotPasswordDialog() {
        _uiState.update { it.copy(showForgotPasswordDialog = true) }
    }

    fun dismissForgotPasswordDialog() {
        _uiState.update { it.copy(showForgotPasswordDialog = false) }
    }

    fun onLogoDoubleTap() {
        val current = _uiState.value
        if (current.isLoading || current.showServerSettingsDialog) return
        viewModelScope.launch {
            val active = networkConfigManager.activeConfig.first()
            _uiState.update {
                it.copy(
                    showServerSettingsDialog = true,
                    serverHostInput = active.host,
                    serverPortInput = active.port.toString(),
                    serverConnectionTestState = ConnectionTestState.IDLE,
                    serverConnectionTestMessage = null,
                    serverHostError = null,
                    serverPortError = null,
                    serverChangeNoticeMessage = null
                )
            }
        }
    }

    fun dismissServerSettingsDialog() {
        _uiState.update {
            it.copy(
                showServerSettingsDialog = false,
                serverConnectionTestState = ConnectionTestState.IDLE,
                serverConnectionTestMessage = null,
                serverHostError = null,
                serverPortError = null,
                serverChangeNoticeMessage = null,
                serverSaveLoading = false
            )
        }
    }

    fun onServerHostChanged(value: String) {
        _uiState.update {
            it.copy(
                serverHostInput = value,
                serverHostError = null,
                serverConnectionTestMessage = null,
                serverConnectionTestState = ConnectionTestState.IDLE
            )
        }
    }

    fun onServerPortChanged(value: String) {
        _uiState.update {
            it.copy(
                serverPortInput = value,
                serverPortError = null,
                serverConnectionTestMessage = null,
                serverConnectionTestState = ConnectionTestState.IDLE
            )
        }
    }

    fun testConnection() {
        val state = _uiState.value
        val hostError = validateHost(state.serverHostInput)
        val portValue = ServerConfig.validatePort(state.serverPortInput)
        val portError = if (portValue == null) "Port harus di antara 1 dan 65535." else null
        if (hostError != null || portError != null) {
            _uiState.update {
                it.copy(
                    serverHostError = hostError,
                    serverPortError = portError
                )
            }
            return
        }
        val host = state.serverHostInput.trim()
        val port = portValue!!
        val targetBase = "http://$host:$port"
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    serverConnectionTestState = ConnectionTestState.TESTING,
                    serverConnectionTestMessage = "Memeriksa koneksi..."
                )
            }
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    runHealthCheck(targetBase)
                }
            }.getOrElse {
                HealthTestResult.Failed("Tidak dapat terhubung ke server.")
            }
            when (result) {
                HealthTestResult.Ok -> {
                    _uiState.update {
                        it.copy(
                            serverConnectionTestState = ConnectionTestState.SUCCESS,
                            serverConnectionTestMessage = "✓ Server Tandara terhubung"
                        )
                    }
                }
                is HealthTestResult.Failed -> {
                    _uiState.update {
                        it.copy(
                            serverConnectionTestState = ConnectionTestState.FAILURE,
                            serverConnectionTestMessage = "Tidak dapat terhubung ke server."
                        )
                    }
                }
            }
        }
    }

    fun saveServer() {
        val state = _uiState.value
        if (state.serverSaveLoading) return
        val hostError = validateHost(state.serverHostInput)
        val portValue = ServerConfig.validatePort(state.serverPortInput)
        val portError = if (portValue == null) "Port harus di antara 1 dan 65535." else null
        if (hostError != null || portError != null) {
            _uiState.update {
                it.copy(
                    serverHostError = hostError,
                    serverPortError = portError
                )
            }
            return
        }
        val host = state.serverHostInput.trim()
        val port = portValue!!
        viewModelScope.launch {
            _uiState.update { it.copy(serverSaveLoading = true) }
            val updateResult = networkConfigManager.updateServer(ServerConfig(host, port))
            when (updateResult) {
                is ConfigUpdateResult.Saved -> {
                    if (updateResult.actuallyChanged) {
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                serverSaveLoading = false,
                                showServerSettingsDialog = false,
                                serverChangeNoticeMessage = "Server berubah. Silakan masuk kembali.",
                                phoneNumber = "",
                                password = "",
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                serverSaveLoading = false,
                                showServerSettingsDialog = false,
                                serverConnectionTestState = ConnectionTestState.IDLE,
                                serverConnectionTestMessage = null
                            )
                        }
                    }
                }
                ConfigUpdateResult.InvalidHost -> {
                    _uiState.update {
                        it.copy(serverSaveLoading = false, serverHostError = "Alamat server tidak valid.")
                    }
                }
                ConfigUpdateResult.InvalidPort -> {
                    _uiState.update {
                        it.copy(serverSaveLoading = false, serverPortError = "Port tidak valid.")
                    }
                }
            }
        }
    }

    private fun validateHost(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return "Alamat server tidak boleh kosong."
        if (!ServerConfig.validateHost(trimmed)) return "Format alamat server tidak valid."
        return null
    }

    fun clearServerChangeNotice() {
        _uiState.update { it.copy(serverChangeNoticeMessage = null) }
    }

    fun login() {
        val current = _uiState.value
        if (current.phoneNumber.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username dan kata sandi wajib diisi.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, serverChangeNoticeMessage = null) }
            val result = authRepository.login(current.phoneNumber, current.password)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is ApiResult.BackendUnavailable -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    private sealed interface HealthTestResult {
        data object Ok : HealthTestResult
        data class Failed(val message: String) : HealthTestResult
    }

    private fun runHealthCheck(baseUrl: String): HealthTestResult {
        val client = OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .writeTimeout(4, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder().url("$baseUrl/api/health").get().build()
        return try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return HealthTestResult.Failed("Server tidak merespons dengan benar.")
                val body = resp.body?.string().orEmpty()
                val status = runCatching {
                    JSONObject(body).optJSONObject("data")?.optString("status").orEmpty()
                }.getOrDefault("")
                if (status.equals("ok", ignoreCase = true)) HealthTestResult.Ok
                else HealthTestResult.Failed("Respons server tidak sesuai.")
            }
        } catch (_: SocketTimeoutException) {
            HealthTestResult.Failed("Koneksi ke server habis waktu.")
        } catch (_: UnknownHostException) {
            HealthTestResult.Failed("Tidak dapat menjangkau alamat server.")
        } catch (_: IOException) {
            HealthTestResult.Failed("Tidak dapat terhubung ke server.")
        } catch (_: Exception) {
            HealthTestResult.Failed("Kesalahan koneksi.")
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val networkConfigManager: NetworkConfigManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository, networkConfigManager) as T
        }
    }
}
