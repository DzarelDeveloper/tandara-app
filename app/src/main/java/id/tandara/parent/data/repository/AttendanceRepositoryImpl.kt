package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.core.network.NetworkDiagnostics
import id.tandara.parent.data.remote.ApiClient
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.session.SessionStore
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
import kotlinx.coroutines.flow.first

class AttendanceRepositoryImpl(
    private val apiClient: ApiClient,
    private val sessionManager: SessionStore,
    private val cache: LocalCacheStore
) : AttendanceRepository {

    private suspend fun accountId() = sessionManager.sessionFlow.first().let { it.parentId.ifBlank { it.username } }

    override suspend fun getTodayAttendance(studentId: String): ApiResult<AttendanceRecord?> {
        val api = apiClient.attendanceApiService
        return try {
            val response = api.getTodayAttendance(studentId)
            if (!response.isSuccessful) {
                val error = apiError(response.code(), response.errorBody()?.string())
                if (error is ApiResult.BackendUnavailable) {
                    return cache.today(accountId(), studentId, LocalDate.now().toString())
                        ?.let { ApiResult.Success(it.value, true, it.fetchedAt) } ?: error
                }
                return error
            }
            val dto = response.body()?.data
            val mapped = dto?.toAttendanceRecord()
            val today = LocalDate.now().toString()
            cache.putToday(accountId(), studentId, today, mapped)
            ApiResult.Success(mapped, lastUpdatedAt = System.currentTimeMillis())
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("GET /api/parent/attendance/today", error)
            val cached = cache.today(accountId(), studentId, LocalDate.now().toString())
            cached?.let { ApiResult.Success(it.value, true, it.fetchedAt) }
                ?: ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("GET /api/parent/attendance/today", error)
            ApiResult.Error("Gagal memuat data presensi hari ini.")
        }
    }

    override suspend fun getMonthlyAttendanceSummary(
        studentId: String,
        month: Int,
        year: Int
    ): ApiResult<AttendanceSummary> {
        val recordsResult = getMonthlyAttendanceRecords(studentId, month, year)
        val records = when (recordsResult) {
            is ApiResult.Success -> recordsResult.data
            is ApiResult.Error -> return recordsResult
            is ApiResult.BackendUnavailable -> return recordsResult
            ApiResult.Loading -> return ApiResult.Error("Data ringkasan belum siap.")
        }
        val summary = AttendanceSummary(
            presentCount = records.count { it.status == AttendanceStatus.PRESENT },
            lateCount = records.count { it.status == AttendanceStatus.LATE },
            permissionCount = records.count { it.status == AttendanceStatus.PERMISSION },
            unexcusedCount = records.count { it.status == AttendanceStatus.UNEXCUSED },
            monthYear = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, Locale("id", "ID")) + " $year"
        )
        return ApiResult.Success(summary, recordsResult.isStale, recordsResult.lastUpdatedAt)
    }

    override suspend fun getMonthlyAttendanceRecords(
        studentId: String,
        month: Int,
        year: Int
    ): ApiResult<List<AttendanceRecord>> {
        val api = apiClient.attendanceApiService
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
                val error = apiError(response.code(), response.errorBody()?.string())
                if (error is ApiResult.BackendUnavailable) {
                    val period = "%04d-%02d".format(year, month)
                    return cache.reports(accountId(), studentId, period)
                        ?.let { ApiResult.Success(it.value, true, it.fetchedAt) } ?: error
                }
                return error
            }
            val items = response.body()?.data?.items ?: emptyList()
            val mapped = items.mapNotNull { it.toAttendanceRecord() }.distinctBy { it.id.ifBlank { it.date } }
            val period = "%04d-%02d".format(year, month)
            cache.putReports(accountId(), studentId, period, mapped)
            ApiResult.Success(mapped, lastUpdatedAt = System.currentTimeMillis())
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("GET /api/parent/attendance/history", error)
            val period = "%04d-%02d".format(year, month)
            cache.reports(accountId(), studentId, period)?.let { ApiResult.Success(it.value, true, it.fetchedAt) }
                ?: ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("GET /api/parent/attendance/history", error)
            ApiResult.Error("Gagal memuat riwayat presensi siswa.")
        }
    }

    private fun apiError(code: Int, raw: String?): ApiResult<Nothing> {
        val message = raw?.takeIf { it.isNotBlank() } ?: "Server Tandara mengembalikan respons yang tidak valid."
        return when (code) {
            401 -> ApiResult.Error("Sesi tidak valid atau telah berakhir.", code)
            403 -> ApiResult.Error("Akses ke data presensi ditolak.", code)
            404 -> ApiResult.Error("Data presensi tidak ditemukan.", code)
            else -> if (code >= 500 || code == 408 || code == 429) {
                ApiResult.BackendUnavailable("Server Tandara sedang tidak tersedia.")
            } else {
                ApiResult.Error(message, code)
            }
        }
    }

    private fun ParentStudentAttendanceDto.toAttendanceRecord(): AttendanceRecord? {
        if (id == null) return null
        val date = date?.let { LocalDate.parse(it) }
        val checkIn = checkInAt?.takeIf { it.isNotBlank() }?.let { parseLocalDateTime(it) }
        val checkOut = checkOutAt?.takeIf { it.isNotBlank() }?.let { parseLocalDateTime(it) }
        return AttendanceRecord(
            id = id.toString(),
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
