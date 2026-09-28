package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.remote.ParentApiService
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.session.SessionManager
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
    private val sessionManager: SessionManager,
    private val api: ParentApiService
) : ParentRepository {

    private suspend fun assignedStudent(): Student? = sessionManager.sessionFlow.first().let { session ->
        if (session.studentId.isBlank()) null else Student(session.studentId, session.studentNis, session.studentName, session.studentClass)
    }

    override suspend fun getAssignedStudent(): ApiResult<Student> {
        return assignedStudent()?.let { ApiResult.Success(it) }
            ?: ApiResult.Error("Akun belum memiliki siswa yang ditugaskan.")
    }

    override suspend fun getLinkedStudents(): ApiResult<List<Student>> {
        return assignedStudent()?.let { ApiResult.Success(listOf(it)) }
            ?: ApiResult.Error("Akun belum memiliki siswa yang ditugaskan.")
    }

    override suspend fun getNotifications(): ApiResult<List<ParentNotification>> {
        return try {
            val response = api.getNotifications()
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            val items = response.body()?.data.orEmpty().map { it.toParentNotification() }
            ApiResult.Success(items)
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal memuat notifikasi orang tua.")
        }
    }

    override suspend fun getUnreadNotificationCount(): ApiResult<Int> {
        return try {
            val response = api.getUnreadNotificationCount()
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            ApiResult.Success(response.body()?.data?.count ?: 0)
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
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
            ApiResult.Success(dto.toParentNotification())
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
            ApiResult.Success(response.body()?.data?.updated ?: 0)
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
            500, 502, 503 -> ApiResult.BackendUnavailable("Server Tandara sedang tidak tersedia.")
            else -> ApiResult.Error(message, code)
        }
    }
}
