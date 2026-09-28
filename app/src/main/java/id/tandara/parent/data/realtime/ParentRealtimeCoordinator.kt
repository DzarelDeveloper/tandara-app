package id.tandara.parent.data.realtime

import android.net.Uri
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.tandara.parent.core.network.NetworkConfig
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.session.SessionManager
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.repository.ParentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val sessionManager: SessionManager,
    private val parentRepository: ParentRepository
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

    private var socket: WebSocket? = null
    private var isListening = false
    private var authFailure = false
    private var reconnectDelayMs = 1000L
    private var activeToken: String? = null

    fun start() {
        if (isListening) return
        isListening = true
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
    }

    fun onAppForeground() {
        scope.launch {
            val token = sessionManager.getAccessToken() ?: return@launch
            val session = sessionManager.sessionFlow.first()
            if (!session.isAuthenticated || authFailure) return@launch
            if (socket == null || !isSocketActive()) {
                connect(token)
            }
            reconcileRestState()
        }
    }

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
        val normalized = NetworkConfig.baseUrl.trimEnd('/')
        val host = normalized.removePrefix("http://").removePrefix("https://")
        val wsScheme = if (NetworkConfig.baseUrl.startsWith("https://")) "wss" else "ws"
        return "$wsScheme://$host/ws/parent?token=${Uri.encode(token)}"
    }

    private fun reconcileRestState() {
        scope.launch {
            parentRepository.getNotifications()
            parentRepository.getUnreadNotificationCount()
        }
    }

    private fun scheduleReconnect() {
        if (authFailure) return
        val delayMs = reconnectDelayMs.coerceAtMost(30_000L)
        reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(30_000L)
        scope.launch {
            delay(delayMs)
            val token = sessionManager.getAccessToken() ?: return@launch
            val session = sessionManager.sessionFlow.first()
            if (session.isAuthenticated && !authFailure) {
                connect(token)
            }
        }
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            reconnectDelayMs = 1000L
            Log.d("ParentRealtimeCoordinator", "Parent WebSocket connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val payload = runCatching { eventAdapter.fromJson(text) }.getOrNull() ?: return
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
            scope.launch {
                parentRepository.getNotifications()
                parentRepository.getUnreadNotificationCount()
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            socket = null
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
            socket = null
            if (t is java.net.UnknownHostException || t is java.net.SocketException || t is java.io.IOException) {
                scheduleReconnect()
                return
            }
            if (t is SecurityException) {
                authFailure = true
                scope.launch { sessionManager.clearSession() }
                return
            }
            Log.w("ParentRealtimeCoordinator", "Parent WebSocket failure", t)
            scheduleReconnect()
        }
    }
}
