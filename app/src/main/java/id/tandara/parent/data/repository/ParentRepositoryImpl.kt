package id.tandara.parent.data.repository

import android.net.Uri
import id.tandara.parent.TandaraApplication
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.core.network.NetworkDiagnostics
import id.tandara.parent.data.remote.ApiClient
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.session.SessionStore
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.model.Student
import id.tandara.parent.domain.repository.ParentRepository
import kotlinx.coroutines.flow.first
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ParentRepositoryImpl(
    private val sessionManager: SessionStore,
    private val apiClient: ApiClient,
    private val cache: LocalCacheStore
) : ParentRepository {

    override suspend fun updateParentPhoto(uri: Uri): ApiResult<String> {
        return try {
            val resolver = TandaraApplication.instance.contentResolver
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return ApiResult.Error("Gambar tidak dapat dibaca.")
            val mime = resolver.getType(uri) ?: "application/octet-stream"
            val response = apiClient.parentApiService.updateProfilePhoto(
                MultipartBody.Part.createFormData("image", "profile", bytes.toRequestBody(mime.toMediaTypeOrNull()))
            )
            if (!response.isSuccessful) return apiError(response.code(), response.errorBody()?.string())
            ApiResult.Success(response.body()?.data?.photoUrl ?: return ApiResult.Error("Respons foto profil tidak valid."))
        } catch (error: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat mengunggah foto ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("POST /api/parent/profile/photo", error)
            ApiResult.Error("Gagal memperbarui foto profil.")
        }
    }

    private suspend fun accountId(): String = sessionManager.sessionFlow.first().let { it.parentId.ifBlank { it.username } }

    override suspend fun cacheRealtimeNotification(notification: ParentNotification) {
        val account = accountId()
        val existing = cache.notifications(account)?.value.orEmpty()
        cache.putNotifications(account, listOf(notification) + existing.filterNot { it.id == notification.id })
    }

    private suspend fun assignedStudent(): Student? = sessionManager.sessionFlow.first().let { session ->
        if (session.studentId.isBlank()) null else Student(session.studentId, session.studentNis, session.studentName, session.studentClass, session.studentPhotoUrl.ifBlank { null })
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
        val api = apiClient.parentApiService
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
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("GET /api/parent/notifications", error)
            cache.notifications(accountId())?.let { ApiResult.Success(it.value, true, it.fetchedAt) }
                ?: ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("GET /api/parent/notifications", error)
            ApiResult.Error("Gagal memuat notifikasi orang tua.")
        }
    }

    override suspend fun getUnreadNotificationCount(): ApiResult<Int> {
        val api = apiClient.parentApiService
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
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("GET /api/parent/notifications/unread-count", error)
            cache.notifications(accountId())?.let { ApiResult.Success(it.value.count { item -> !item.isRead }, true, it.fetchedAt) }
                ?: ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("GET /api/parent/notifications/unread-count", error)
            ApiResult.Error("Gagal menghitung notifikasi belum dibaca.")
        }
    }

    override suspend fun markNotificationRead(notificationId: String): ApiResult<ParentNotification> {
        val api = apiClient.parentApiService
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
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("PATCH /api/parent/notifications", error)
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("PATCH /api/parent/notifications", error)
            ApiResult.Error("Gagal menandai notifikasi sebagai dibaca.")
        }
    }

    override suspend fun markAllNotificationsRead(): ApiResult<Int> {
        val api = apiClient.parentApiService
        return try {
            val response = api.markAllNotificationsRead()
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            val updated = response.body()?.data?.updated ?: 0
            cache.notifications(accountId())?.value?.let { values -> cache.putNotifications(accountId(), values.map { it.copy(isRead = true) }) }
            ApiResult.Success(updated)
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("PATCH /api/parent/notifications/read-all", error)
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("PATCH /api/parent/notifications/read-all", error)
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
