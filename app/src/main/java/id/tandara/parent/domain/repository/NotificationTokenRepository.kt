package id.tandara.parent.domain.repository

import id.tandara.parent.core.network.ApiResult

interface NotificationTokenRepository {
    suspend fun registerDeviceToken(token: String): ApiResult<Unit>
    suspend fun getDeviceToken(): String?
}
