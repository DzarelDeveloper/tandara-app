package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.domain.repository.NotificationTokenRepository

class NotificationTokenRepositoryImpl : NotificationTokenRepository {
    override suspend fun registerDeviceToken(token: String): ApiResult<Unit> {
        // Future FCM token registration with FastAPI school backend
        return ApiResult.BackendUnavailable("FCM token registration requires connected school server.")
    }

    override suspend fun getDeviceToken(): String? {
        return null
    }
}
