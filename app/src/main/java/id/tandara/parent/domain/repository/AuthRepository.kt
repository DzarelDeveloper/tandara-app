package id.tandara.parent.domain.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.domain.model.Parent
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentParentSession: Flow<Parent?>
    suspend fun login(username: String, password: String): ApiResult<Parent>
    suspend fun validateSession(): ApiResult<Parent>
    suspend fun logout()
    suspend fun changePassword(current: String, new: String): ApiResult<Unit>
}
