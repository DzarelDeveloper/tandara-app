package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.remote.PermissionApiService
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.session.SessionStore
import id.tandara.parent.data.remote.dto.CreateLeaveRequestDto
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.LeaveStatus
import id.tandara.parent.domain.model.LeaveType
import id.tandara.parent.domain.repository.PermissionRepository
import java.io.IOException
import kotlinx.coroutines.flow.first

class PermissionRepositoryImpl(
    private val api: PermissionApiService,
    private val sessionManager: SessionStore,
    private val cache: LocalCacheStore
) : PermissionRepository {

    private suspend fun accountId() = sessionManager.sessionFlow.first().let { it.parentId.ifBlank { it.username } }

    override suspend fun submitLeaveRequest(request: LeaveRequest): ApiResult<LeaveRequest> {
        return try {
            val response = api.submitLeaveRequest(
                CreateLeaveRequestDto(
                    studentId = request.studentId.toIntOrNull() ?: return ApiResult.Error("Identitas siswa tidak valid."),
                    leaveDate = request.startDate,
                    leaveType = request.type.name,
                    reason = request.reason
                )
            )
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            val body = response.body()?.data ?: return ApiResult.Error("Respons server izin tidak valid.")
                val saved = request.copy(
                    id = body.id.toString(),
                    status = LeaveStatus.fromBackend(body.status),
                        submittedAt = ""
                )
            val existing = cache.leave(accountId(), request.studentId)?.value.orEmpty()
            cache.putLeave(accountId(), request.studentId, listOf(saved) + existing.filterNot { it.id == saved.id })
            ApiResult.Success(saved)
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal mengirim pengajuan izin.")
        }
    }

    override suspend fun getLeaveHistory(studentId: String): ApiResult<List<LeaveRequest>> {
        return try {
            val response = api.getLeaveHistory()
            if (!response.isSuccessful) {
                val error = apiError(response.code(), response.errorBody()?.string())
                if (error is ApiResult.BackendUnavailable) {
                    return cache.leave(accountId(), studentId)?.let { ApiResult.Success(it.value, true, it.fetchedAt) } ?: error
                }
                return error
            }
            val items = response.body()?.data.orEmpty().filter { item ->
                item.studentId?.toString() == studentId
            }
            val mapped = items.map { item ->
                LeaveRequest(
                    id = item.id.toString(),
                    studentId = item.studentId?.toString() ?: studentId,
                    type = LeaveType.fromBackend(item.leaveType),
                    startDate = item.leaveDate ?: "",
                    endDate = item.leaveDate ?: "",
                    reason = item.reason ?: "",
                    status = LeaveStatus.fromBackend(item.status),
                    submittedAt = item.createdAt ?: ""
                )
            }.distinctBy { it.id }
            cache.putLeave(accountId(), studentId, mapped)
            ApiResult.Success(mapped, lastUpdatedAt = System.currentTimeMillis())
        } catch (_: IOException) {
            cache.leave(accountId(), studentId)?.let { ApiResult.Success(it.value, true, it.fetchedAt) }
                ?: ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal memuat riwayat izin.")
        }
    }

    private fun apiError(code: Int, raw: String?): ApiResult<Nothing> {
        val message = raw?.takeIf { it.isNotBlank() } ?: "Server Tandara mengembalikan respons yang tidak valid."
        return when (code) {
            401 -> ApiResult.Error("Sesi tidak valid atau telah berakhir.", code)
            403 -> ApiResult.Error("Akses ke data izin ditolak.", code)
            404 -> ApiResult.Error("Data izin tidak ditemukan.", code)
            else -> if (code >= 500 || code == 408 || code == 429) {
                ApiResult.BackendUnavailable("Server Tandara sedang tidak tersedia.")
            } else {
                ApiResult.Error(message, code)
            }
        }
    }
}
