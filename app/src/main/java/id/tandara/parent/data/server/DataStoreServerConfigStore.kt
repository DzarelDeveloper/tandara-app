package id.tandara.parent.data.server

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.tandara.parent.core.network.ServerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.serverDataStore: DataStore<Preferences> by preferencesDataStore(name = "tandara_server_config")

interface ServerConfigStore {
    val configFlow: Flow<ServerConfig>
    suspend fun getCurrent(): ServerConfig
    suspend fun save(config: ServerConfig)
}

class DataStoreServerConfigStore(private val context: Context) : ServerConfigStore {

    companion object {
        private val KEY_HOST = stringPreferencesKey("server_host")
        private val KEY_PORT = intPreferencesKey("server_port")
    }

    override val configFlow: Flow<ServerConfig> = context.serverDataStore.data.map { prefs ->
        val storedHost = prefs[KEY_HOST]
        val storedPort = prefs[KEY_PORT]
        if (storedHost.isNullOrBlank() || storedPort == null) {
            ServerConfig.fromBuildConfigFallback()
        } else {
            ServerConfig(storedHost, storedPort.coerceIn(1, 65535))
        }
    }

    override suspend fun getCurrent(): ServerConfig = configFlow.first()

    override suspend fun save(config: ServerConfig) {
        val host = config.host.trim()
        val port = config.port.coerceIn(1, 65535)
        context.serverDataStore.edit { prefs ->
            prefs[KEY_HOST] = host
            prefs[KEY_PORT] = port
        }
    }
}
