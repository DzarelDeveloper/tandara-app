package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.remote.PermissionApiService
import id.tandara.parent.data.remote.dto.CreateLeaveRequestDto
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.LeaveStatus
import id.tandara.parent.domain.model.LeaveType
import id.tandara.parent.domain.repository.PermissionRepository
import java.io.IOException

class PermissionRepositoryImpl(
    private val api: PermissionApiService
) : PermissionRepository {

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
            ApiResult.Success(
                request.copy(
                    id = body.id,
                    status = LeaveStatus.fromBackend(body.status),
                    submittedAt = body.status ?: ""
                )
            )
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
                return apiError(response.code(), response.errorBody()?.string())
            }
            val items = response.body()?.data.orEmpty().filter { item ->
                item.studentId?.toString() == studentId || item.studentId == studentId
            }
            ApiResult.Success(items.map { item ->
                LeaveRequest(
                    id = item.id,
                    studentId = item.studentId ?: studentId,
                    type = LeaveType.fromBackend(item.leaveType),
                    startDate = item.leaveDate ?: "",
                    endDate = item.leaveDate ?: "",
                    reason = item.reason ?: "",
                    status = LeaveStatus.fromBackend(item.status),
                    submittedAt = item.createdAt ?: ""
                )
            })
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
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
            500, 502, 503 -> ApiResult.BackendUnavailable("Server Tandara sedang tidak tersedia.")
            else -> ApiResult.Error(message, code)
        }
    }
}
