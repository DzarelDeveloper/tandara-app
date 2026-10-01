package id.tandara.parent.domain.repository

import android.net.Uri
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.model.Student

/**
 * Repository for authenticated Parent account.
 * Enforces strict rule: ONE PARENT ACCOUNT = ONE STUDENT.
 */
interface ParentRepository {
    suspend fun getAssignedStudent(): ApiResult<Student>
    suspend fun getLinkedStudents(): ApiResult<List<Student>>
    suspend fun getNotifications(): ApiResult<List<ParentNotification>>
    suspend fun getUnreadNotificationCount(): ApiResult<Int>
    suspend fun markNotificationRead(notificationId: String): ApiResult<ParentNotification>
    suspend fun markAllNotificationsRead(): ApiResult<Int>
    suspend fun cacheRealtimeNotification(notification: ParentNotification)
    suspend fun updateParentPhoto(uri: Uri): ApiResult<String>
}
