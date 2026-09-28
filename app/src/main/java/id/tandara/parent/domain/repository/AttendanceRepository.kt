package id.tandara.parent.domain.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.AttendanceSummary

interface AttendanceRepository {
    suspend fun getTodayAttendance(studentId: String): ApiResult<AttendanceRecord?>
    suspend fun getMonthlyAttendanceSummary(studentId: String, month: Int, year: Int): ApiResult<AttendanceSummary>
    suspend fun getMonthlyAttendanceRecords(studentId: String, month: Int, year: Int): ApiResult<List<AttendanceRecord>>
}
