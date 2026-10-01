package id.tandara.parent.data.realtime

import android.net.Uri
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.core.network.NetworkConfigManager
import id.tandara.parent.core.network.NetworkDiagnostics
import id.tandara.parent.core.network.NetworkMonitor
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.session.SessionStore
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.repository.AuthRepository
import id.tandara.parent.domain.repository.ParentRepository
import id.tandara.parent.core.common.SystemNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

private data class ParentRealtimeEnvelope(
    val type: String? = null,
    val notification: NotificationDto? = null
)

data class ParentRealtimeEvent(
    val type: String,
    val notification: ParentNotification
)

class ParentRealtimeCoordinator(
    private val sessionManager: SessionStore,
    private val authRepository: AuthRepository,
    private val parentRepository: ParentRepository,
    private val networkMonitor: NetworkMonitor,
    private val networkConfigManager: NetworkConfigManager,
    private val systemNotificationManager: SystemNotificationManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val eventAdapter = moshi.adapter(ParentRealtimeEnvelope::class.java)

    private val _events = MutableSharedFlow<ParentRealtimeEvent>(
        replay = 0,
        extraBufferCapacity = 32
    )
    val events: SharedFlow<ParentRealtimeEvent> = _events.asSharedFlow()
    private val _refreshEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshEvents: SharedFlow<Unit> = _refreshEvents.asSharedFlow()
    val networkConnected = networkMonitor.isConnected
    private val reconciliationMutex = Mutex()

    private var socket: WebSocket? = null
    private var isListening = false
    private var authFailure = false
    private var reconnectDelayMs = 1000L
    private var activeToken: String? = null
    @Volatile private var reconnectScheduled = false
    private val _appForeground = MutableStateFlow(false)
    val isAppForeground: StateFlow<Boolean> = _appForeground.asStateFlow()
    private val appForeground: Boolean
        get() = _appForeground.value

    fun start() {
        if (isListening) return
        isListening = true
        scope.launch {
            networkConfigManager.serverChangedEvents.collect {
                authFailure = false
                disconnectSocket()
                activeToken = null
            }
        }
        scope.launch {
            sessionManager.sessionFlow.collect { session ->
                val token = sessionManager.getAccessToken()
                if (!session.isAuthenticated || token.isNullOrBlank()) {
                    authFailure = false
                    disconnectSocket()
                    activeToken = null
                    return@collect
                }

                if (authFailure) return@collect
                if (token != activeToken || socket == null) {
                    activeToken = token
                    connect(token)
                }
            }
        }
        scope.launch {
            networkMonitor.isConnected.collect { connected ->
                if (!connected) {
                    reconnectScheduled = false
                    disconnectSocket()
                } else {
                    onAppForeground()
                }
            }
        }
    }

    fun onAppForeground() {
        _appForeground.value = true
        scope.launch {
            val token = sessionManager.getAccessToken() ?: return@launch
            val session = sessionManager.sessionFlow.first()
            if (!session.isAuthenticated || authFailure) return@launch
            if (reconcileRestState()) {
                val refreshedToken = sessionManager.getAccessToken() ?: return@launch
                connect(refreshedToken)
            }
        }
    }

    fun onAppBackground() {
        _appForeground.value = false
    }

    @Synchronized
    private fun connect(token: String) {
        if (authFailure) return
        if (socket != null && isSocketActive()) return

        val request = Request.Builder()
            .url(buildWebSocketUrl(token))
            .build()

        socket = okHttpClient.newWebSocket(request, socketListener)
    }

    private fun disconnectSocket() {
        socket?.close(1000, null)
        socket = null
        activeToken = null
    }

    private fun isSocketActive(): Boolean {
        return socket != null
    }

    private fun buildWebSocketUrl(token: String): String {
        val baseUrl = networkConfigManager.baseHttpUrl
        val normalized = baseUrl.trimEnd('/')
        val host = normalized.removePrefix("http://").removePrefix("https://")
        val wsScheme = if (baseUrl.startsWith("https://")) "wss" else "ws"
        return "$wsScheme://$host/ws/parent?token=${Uri.encode(token)}"
    }

    private suspend fun reconcileRestState(): Boolean {
        if (!networkMonitor.isConnected.value || !reconciliationMutex.tryLock()) return false
        try {
            val sessionResult = authRepository.validateSession()
            if (sessionResult is ApiResult.Error && sessionResult.code in setOf(401, 403)) {
                authFailure = true
                disconnectSocket()
                return false
            }

            val session = sessionManager.sessionFlow.first()
            if (!session.isAuthenticated) return false
            _refreshEvents.emit(Unit)
            return true
        } finally {
            reconciliationMutex.unlock()
        }
    }

    private fun scheduleReconnect() {
        if (authFailure || reconnectScheduled || !networkMonitor.isConnected.value) return
        reconnectScheduled = true
        val delayMs = reconnectDelayMs.coerceAtMost(30_000L)
        reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(30_000L)
        scope.launch {
            delay(delayMs)
            reconnectScheduled = false
            if (!authFailure && appForeground && reconcileRestState()) {
                val token = sessionManager.getAccessToken() ?: return@launch
                connect(token)
            }
        }
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            reconnectDelayMs = 1000L
            reconnectScheduled = false
            Log.d("ParentRealtimeCoordinator", "Parent WebSocket connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val payload = runCatching { eventAdapter.fromJson(text) }
                .onFailure { NetworkDiagnostics.logFailure("WebSocket /ws/parent", it) }
                .getOrNull() ?: return
            val notificationDto = payload.notification ?: return
            val notification = ParentNotification(
                id = notificationDto.id.toString(),
                title = notificationDto.title.ifBlank { "Notifikasi" },
                message = notificationDto.message.ifBlank { "Informasi terbaru tersedia." },
                timestamp = notificationDto.createdAt.ifBlank { "Baru" },
                isRead = notificationDto.isRead,
                relatedStudentId = notificationDto.student?.id?.toString(),
                relatedStudentName = notificationDto.student?.fullName,
                type = notificationDto.type
            )
            val eventType = payload.type ?: notificationDto.type ?: "UNKNOWN_EVENT"
            if (eventType == "UNKNOWN_EVENT") return
            _events.tryEmit(ParentRealtimeEvent(type = eventType, notification = notification))
            systemNotificationManager.show(eventType, notification)
            scope.launch {
                parentRepository.cacheRealtimeNotification(notification)
                parentRepository.getNotifications()
                parentRepository.getUnreadNotificationCount()
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (socket === webSocket) socket = null
            if (code == 1008) {
                authFailure = true
                scope.launch {
                    sessionManager.clearSession()
                }
                Log.w("ParentRealtimeCoordinator", "Parent WebSocket authentication failed; session cleared")
                return
            }
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            if (socket === webSocket) socket = null
            if (response != null) {
                NetworkDiagnostics.logHttpStatus("GET", "/ws/parent", response.code)
            } else {
                NetworkDiagnostics.logFailure("WebSocket /ws/parent", t)
            }
            if (response?.code in setOf(401, 403)) {
                authFailure = true
                scope.launch { sessionManager.clearSession() }
                Log.w("ParentRealtimeCoordinator", "Parent WebSocket authentication failed; session cleared")
                return
            }
            if (t is java.net.UnknownHostException || t is java.net.SocketException || t is java.io.IOException) {
                scheduleReconnect()
                return
            }
            if (t is SecurityException) {
                Log.w("ParentRealtimeCoordinator", "Parent WebSocket permission or configuration failure", t)
                return
            }
            Log.w("ParentRealtimeCoordinator", "Parent WebSocket failure", t)
            scheduleReconnect()
        }
    }
}
