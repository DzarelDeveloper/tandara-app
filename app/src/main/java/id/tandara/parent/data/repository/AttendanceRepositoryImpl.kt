package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.remote.AttendanceApiService
import id.tandara.parent.data.remote.dto.ParentStudentAttendanceDto
import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.domain.model.AttendanceSummary
import id.tandara.parent.domain.repository.AttendanceRepository
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AttendanceRepositoryImpl(
    private val api: AttendanceApiService
) : AttendanceRepository {

    override suspend fun getTodayAttendance(studentId: String): ApiResult<AttendanceRecord?> {
        return try {
            val response = api.getTodayAttendance(studentId)
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            val dto = response.body()?.data ?: return ApiResult.Success(null)
            ApiResult.Success(dto.toAttendanceRecord())
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal memuat data presensi hari ini.")
        }
    }

    override suspend fun getMonthlyAttendanceSummary(
        studentId: String,
        month: Int,
        year: Int
    ): ApiResult<AttendanceSummary> {
        val records = when (val result = getMonthlyAttendanceRecords(studentId, month, year)) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> return result
            is ApiResult.BackendUnavailable -> return result
            ApiResult.Loading -> return ApiResult.Error("Data ringkasan belum siap.")
        }
        val summary = AttendanceSummary(
            presentCount = records.count { it.status == AttendanceStatus.PRESENT },
            lateCount = records.count { it.status == AttendanceStatus.LATE },
            permissionCount = records.count { it.status == AttendanceStatus.PERMISSION },
            unexcusedCount = records.count { it.status == AttendanceStatus.UNEXCUSED },
            monthYear = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, Locale("id", "ID")) + " $year"
        )
        return ApiResult.Success(summary)
    }

    override suspend fun getMonthlyAttendanceRecords(
        studentId: String,
        month: Int,
        year: Int
    ): ApiResult<List<AttendanceRecord>> {
        return try {
            val start = LocalDate.of(year, month, 1)
            val end = start.withDayOfMonth(start.lengthOfMonth())
            val response = api.getHistory(
                studentId = studentId,
                dateFrom = start.toString(),
                dateTo = end.toString(),
                pageSize = 100
            )
            if (!response.isSuccessful) {
                return apiError(response.code(), response.errorBody()?.string())
            }
            val items = response.body()?.data?.items ?: emptyList()
            ApiResult.Success(items.map { it.toAttendanceRecord() })
        } catch (_: IOException) {
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) {
            ApiResult.Error("Gagal memuat riwayat presensi siswa.")
        }
    }

    private fun apiError(code: Int, raw: String?): ApiResult<Nothing> {
        val message = raw?.takeIf { it.isNotBlank() } ?: "Server Tandara mengembalikan respons yang tidak valid."
        return when (code) {
            401 -> ApiResult.Error("Sesi tidak valid atau telah berakhir.", code)
            403 -> ApiResult.Error("Akses ke data presensi ditolak.", code)
            404 -> ApiResult.Error("Data presensi tidak ditemukan.", code)
            500, 502, 503 -> ApiResult.BackendUnavailable("Server Tandara sedang tidak tersedia.")
            else -> ApiResult.Error(message, code)
        }
    }

    private fun ParentStudentAttendanceDto.toAttendanceRecord(): AttendanceRecord {
        val date = date?.let { LocalDate.parse(it) }
        val checkIn = checkInAt?.takeIf { it.isNotBlank() }?.let { parseLocalDateTime(it) }
        val checkOut = checkOutAt?.takeIf { it.isNotBlank() }?.let { parseLocalDateTime(it) }
        return AttendanceRecord(
            id = id ?: "",
            date = date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))) ?: "-",
            dayName = date?.dayOfWeek?.getDisplayName(java.time.format.TextStyle.FULL, Locale("id", "ID")) ?: "-",
            checkInTime = checkIn?.format(DateTimeFormatter.ofPattern("HH:mm")),
            checkOutTime = checkOut?.format(DateTimeFormatter.ofPattern("HH:mm")),
            status = AttendanceStatus.fromBackend(status),
            note = notes
        )
    }

    private fun parseLocalDateTime(value: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
        } catch (_: Exception) {
            try {
                LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
            } catch (_: Exception) {
                null
            }
        }
    }
}
