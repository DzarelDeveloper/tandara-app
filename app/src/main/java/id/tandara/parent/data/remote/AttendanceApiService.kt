package id.tandara.parent.data.remote

import id.tandara.parent.data.remote.dto.ApiEnvelope
import id.tandara.parent.data.remote.dto.ParentAttendanceHistoryDto
import id.tandara.parent.data.remote.dto.ParentStudentAttendanceDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceApiService {
    @GET("api/parent/students/{student_id}/attendance/today")
    suspend fun getTodayAttendance(
        @Path("student_id") studentId: String
    ): Response<ApiEnvelope<ParentStudentAttendanceDto>>

    @GET("api/parent/students/{student_id}/attendance")
    suspend fun getHistory(
        @Path("student_id") studentId: String,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100
    ): Response<ApiEnvelope<ParentAttendanceHistoryDto>>
}
