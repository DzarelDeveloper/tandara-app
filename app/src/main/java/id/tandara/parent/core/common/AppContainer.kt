package id.tandara.parent.core.common

import android.content.Context
import id.tandara.parent.core.network.NetworkConfigManager
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.local.TandaraCacheDatabase
import id.tandara.parent.data.realtime.ParentRealtimeCoordinator
import id.tandara.parent.data.remote.ApiClient
import id.tandara.parent.data.repository.AttendanceRepositoryImpl
import id.tandara.parent.data.repository.AuthRepositoryImpl
import id.tandara.parent.data.repository.ParentRepositoryImpl
import id.tandara.parent.data.repository.PermissionRepositoryImpl
import id.tandara.parent.data.server.DataStoreServerConfigStore
import id.tandara.parent.data.server.ServerConfigStore
import id.tandara.parent.data.session.SessionManager
import id.tandara.parent.core.network.NetworkMonitor
import id.tandara.parent.domain.repository.AttendanceRepository
import id.tandara.parent.domain.repository.AuthRepository
import id.tandara.parent.domain.repository.ParentRepository
import id.tandara.parent.domain.repository.PermissionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

interface AppContainer {
    val sessionManager: SessionManager
    val authRepository: AuthRepository
    val parentRepository: ParentRepository
    val attendanceRepository: AttendanceRepository
    val permissionRepository: PermissionRepository
    val notificationPermissionManager: NotificationPermissionManager
    val systemNotificationManager: SystemNotificationManager
    val parentRealtimeCoordinator: ParentRealtimeCoordinator
    val networkMonitor: NetworkMonitor
    val serverConfigStore: ServerConfigStore
    val networkConfigManager: NetworkConfigManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    override val serverConfigStore: ServerConfigStore by lazy {
        DataStoreServerConfigStore(context.applicationContext)
    }

    override val networkConfigManager: NetworkConfigManager by lazy {
        NetworkConfigManager(serverConfigStore)
    }

    private val apiClient: ApiClient by lazy { ApiClient(sessionManager, networkConfigManager) }
    private val database by lazy { TandaraCacheDatabase.create(context) }
    private val cache by lazy { LocalCacheStore(database.cacheDao()) }
    override val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(context) }

    private val containerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        containerScope.launch {
            networkConfigManager.serverChangedEvents
                .filter { it.previous.normalizedKey() != it.new.normalizedKey() }
                .collect {
                    apiClient.invalidateServices()
                }
        }
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(sessionManager, apiClient, cache)
    }

    override val parentRepository: ParentRepository by lazy {
        ParentRepositoryImpl(sessionManager, apiClient, cache)
    }

    override val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(apiClient, sessionManager, cache)
    }

    override val permissionRepository: PermissionRepository by lazy {
        PermissionRepositoryImpl(apiClient, sessionManager, cache)
    }

    override val notificationPermissionManager: NotificationPermissionManager by lazy {
        NotificationPermissionManager(context)
    }
    override val systemNotificationManager: SystemNotificationManager by lazy { SystemNotificationManager(context) }

    override val parentRealtimeCoordinator: ParentRealtimeCoordinator by lazy {
        ParentRealtimeCoordinator(sessionManager, authRepository, parentRepository, networkMonitor, networkConfigManager, systemNotificationManager)
    }
}
