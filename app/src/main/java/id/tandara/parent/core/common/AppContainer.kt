package id.tandara.parent.core.common

import android.content.Context
import id.tandara.parent.data.realtime.ParentRealtimeCoordinator
import id.tandara.parent.data.repository.AttendanceRepositoryImpl
import id.tandara.parent.data.repository.AuthRepositoryImpl
import id.tandara.parent.data.repository.ParentRepositoryImpl
import id.tandara.parent.data.repository.PermissionRepositoryImpl
import id.tandara.parent.data.remote.ApiClient
import id.tandara.parent.data.session.SessionManager
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.local.TandaraCacheDatabase
import id.tandara.parent.core.network.NetworkMonitor
import id.tandara.parent.domain.repository.AttendanceRepository
import id.tandara.parent.domain.repository.AuthRepository
import id.tandara.parent.domain.repository.ParentRepository
import id.tandara.parent.domain.repository.PermissionRepository

interface AppContainer {
    val sessionManager: SessionManager
    val authRepository: AuthRepository
    val parentRepository: ParentRepository
    val attendanceRepository: AttendanceRepository
    val permissionRepository: PermissionRepository
    val notificationPermissionManager: NotificationPermissionManager
    val parentRealtimeCoordinator: ParentRealtimeCoordinator
    val networkMonitor: NetworkMonitor
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    private val apiClient: ApiClient by lazy { ApiClient(sessionManager) }
    private val database by lazy { TandaraCacheDatabase.create(context) }
    private val cache by lazy { LocalCacheStore(database.cacheDao()) }
    override val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(context) }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(sessionManager, apiClient.authApiService, cache)
    }

    override val parentRepository: ParentRepository by lazy {
        ParentRepositoryImpl(sessionManager, apiClient.parentApiService, cache)
    }

    override val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(apiClient.attendanceApiService, sessionManager, cache)
    }

    override val permissionRepository: PermissionRepository by lazy {
        PermissionRepositoryImpl(apiClient.permissionApiService, sessionManager, cache)
    }

    override val notificationPermissionManager: NotificationPermissionManager by lazy {
        NotificationPermissionManager(context)
    }

    override val parentRealtimeCoordinator: ParentRealtimeCoordinator by lazy {
        ParentRealtimeCoordinator(sessionManager, authRepository, parentRepository, networkMonitor)
    }
}
