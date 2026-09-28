package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.remote.ParentApiService
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.session.SessionStore
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.model.Student
import id.tandara.parent.domain.repository.ParentRepository
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Implementation of ParentRepository.
 * Strictly resolves exactly ONE assigned student for the authenticated parent.
 */
class ParentRepositoryImpl(
    private val sessionManager: SessionStore,
    private val api: ParentApiService,
    private val cache: LocalCacheStore
) : ParentRepository {

    private suspend fun accountId(): String = sessionManager.sessionFlow.first().let { it.parentId.ifBlank { it.username } }

    override suspend fun cacheRealtimeNotification(notification: ParentNotification) {
        val account = accountId()
        val existing = cache.notifications(account)?.value.orEmpty()
        cache.putNotifications(account, listOf(notification) + existing.filterNot { it.id == notification.id })
    }

    private suspend fun assignedStudent(): Student? = sessionManager.sessionFlow.first().let { session ->
        if (session.studentId.isBlank()) null else Student(session.studentId, session.studentNis, session.studentName, session.studentClass)
    }

    override suspend fun getAssignedStudent(): ApiResult<Student> {
        val account = accountId()
        return assignedStudent()?.also { cache.putStudent(account, it) }?.let { ApiResult.Success(it) }
            ?: cache.student(account)?.let { ApiResult.Success(it.value, true, it.fetchedAt) }
            ?: ApiResult.Error("Akun belum memiliki siswa yang ditugaskan.")
    }

    override suspend fun getLinkedStudents(): ApiResult<List<Student>> {
        return when (val result = getAssignedStudent()) {
            is ApiResult.Success -> ApiResult.Success(listOf(result.data), result.isStale, result.lastUpdatedAt)
            is ApiResult.Error -> result
            is ApiResult.BackendUnavailable -> result
            ApiResult.Loading -> ApiResult.Loading
        }
    }

    override suspend fun getNotifications(): ApiResult<List<ParentNotification>> {
        return try {
            val response = api.getNotifications()
            if (!response.isSuccessful) {
                val error = apiError(response.code(), response.errorBody()?.string())
                if (error is ApiResult.BackendUnavailable) {
                    return cache.notifications(accountId())?.let { ApiResult.Success(it.value, true, it.fetchedAt) } ?: error
                }
                return error
            }
            val items = response.body()?.data?.items.orEmpty().map { it.toParentNotification() }
            cache.putNotifications(accountId(), items)
            ApiResult.Success(items, lastUpdatedAt = System.currentTimeMillis())
        } catch (_: IOException) {
            cache.notifications(accountId())?.let { ApiResult.Success(it.value, true, it.fetchedAt) }
                ?: ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal memuat notifikasi orang tua.")
        }
    }

    override suspend fun getUnreadNotificationCount(): ApiResult<Int> {
        return try {
            val response = api.getUnreadNotificationCount()
            if (!response.isSuccessful) {
                val error = apiError(response.code(), response.errorBody()?.string())
                if (error is ApiResult.BackendUnavailable) {
                    return cache.notifications(accountId())?.let { ApiResult.Success(it.value.count { item -> !item.isRead }, true, it.fetchedAt) } ?: error
                }
                return error
            }
            ApiResult.Success(response.body()?.data?.count ?: 0)
        } catch (_: IOException) {
            cache.notifications(accountId())?.let { ApiResult.Success(it.value.count { item -> !item.isRead }, true, it.fetchedAt) }
                ?: ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal menghitung notifikasi belum dibaca.")
        }
    }

    override suspend fun markNotificationRead(notificationId: String): ApiResult<ParentNotification> {
        return try {
            val response = api.markNotificationRead(notificationId.toIntOrNull() ?: return ApiResult.Error("ID notifikasi tidak valid."))
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            val dto = response.body()?.data ?: return ApiResult.Error("Respons notifikasi tidak valid.")
            val mapped = dto.toParentNotification()
            val current = cache.notifications(accountId())?.value.orEmpty()
            cache.putNotifications(accountId(), listOf(mapped) + current.filterNot { it.id == mapped.id })
            ApiResult.Success(mapped)
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal menandai notifikasi sebagai dibaca.")
        }
    }

    override suspend fun markAllNotificationsRead(): ApiResult<Int> {
        return try {
            val response = api.markAllNotificationsRead()
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            val updated = response.body()?.data?.updated ?: 0
            cache.notifications(accountId())?.value?.let { values -> cache.putNotifications(accountId(), values.map { it.copy(isRead = true) }) }
            ApiResult.Success(updated)
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal menandai semua notifikasi sebagai dibaca.")
        }
    }

    private fun NotificationDto.toParentNotification(): ParentNotification {
        return ParentNotification(
            id = id.toString(),
            title = title.ifBlank { "Notifikasi" },
            message = message.ifBlank { "Informasi terbaru tersedia." },
            timestamp = createdAt,
            isRead = isRead,
            relatedStudentId = student?.id?.toString(),
            relatedStudentName = student?.fullName,
            type = type
        )
    }

    private fun apiError(code: Int, raw: String?): ApiResult<Nothing> {
        val message = raw?.takeIf { it.isNotBlank() } ?: "Server Tandara mengembalikan respons yang tidak valid."
        return when (code) {
            401 -> ApiResult.Error("Sesi tidak valid atau telah berakhir.", code)
            403 -> ApiResult.Error("Akses ke notifikasi ditolak.", code)
            404 -> ApiResult.Error("Notifikasi tidak ditemukan.", code)
            else -> if (code >= 500 || code == 408 || code == 429) {
                ApiResult.BackendUnavailable("Server Tandara sedang tidak tersedia.")
            } else {
                ApiResult.Error(message, code)
            }
        }
    }
}
