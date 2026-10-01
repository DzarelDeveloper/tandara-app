package id.tandara.parent.core.network

import id.tandara.parent.data.server.ServerConfigStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class NetworkConfigManager(
    private val store: ServerConfigStore
) {
    private val _active = MutableStateFlow<ServerConfig>(runBlocking { store.getCurrent() })
    val activeConfig: StateFlow<ServerConfig> = _active.asStateFlow()

    private val _serverChangedEvents = MutableSharedFlow<ServerConfigChange>(extraBufferCapacity = 8)
    val serverChangedEvents: SharedFlow<ServerConfigChange> = _serverChangedEvents.asSharedFlow()

    val baseHttpUrl: String get() = _active.value.resolvedHttpBaseUrl
    val baseWsUrl: String get() = _active.value.resolvedWebSocketBaseUrl
    val currentHost: String get() = _active.value.host.trim()
    val currentPort: Int get() = _active.value.port.coerceIn(1, 65535)

    suspend fun reloadFromStore() {
        val loaded = store.getCurrent()
        if (loaded.normalizedKey() != _active.value.normalizedKey()) {
            _active.value = loaded
        }
    }

    suspend fun updateServer(newConfig: ServerConfig, requireSuccessful: Boolean = false): ConfigUpdateResult {
        if (!ServerConfig.validateHost(newConfig.host)) {
            return ConfigUpdateResult.InvalidHost
        }
        val port = ServerConfig.validatePort(newConfig.port.toString())
        if (port == null) {
            return ConfigUpdateResult.InvalidPort
        }
        val normalized = ServerConfig(newConfig.host.trim(), port)
        val previous = _active.value
        val changed = normalized.normalizedKey() != previous.normalizedKey()
        store.save(normalized)
        _active.value = normalized
        if (changed) {
            _serverChangedEvents.tryEmit(ServerConfigChange(previous, normalized))
        }
        return ConfigUpdateResult.Saved(changed, requireSuccessful)
    }

    fun sameAs(host: String, port: Int): Boolean {
        val cleanedHost = host.trim().lowercase()
        val cleanedPort = port.coerceIn(1, 65535)
        return _active.value.host.trim().lowercase() == cleanedHost &&
            _active.value.port.coerceIn(1, 65535) == cleanedPort
    }
}

data class ServerConfigChange(
    val previous: ServerConfig,
    val new: ServerConfig
)

sealed interface ConfigUpdateResult {
    data object InvalidHost : ConfigUpdateResult
    data object InvalidPort : ConfigUpdateResult
    data class Saved(val actuallyChanged: Boolean, val requireConnectionTest: Boolean) : ConfigUpdateResult
}
