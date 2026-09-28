package id.tandara.parent.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StudentDto(
    @Json(name = "id") val id: String,
    @Json(name = "nis") val nis: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "class") val className: String,
    @Json(name = "grade") val grade: String? = null,
    @Json(name = "major") val major: String? = null,
    @Json(name = "face_enrollment_status") val faceEnrollmentStatus: String? = null
)

@JsonClass(generateAdapter = true)
data class ParentStudentAttendanceDto(
    @Json(name = "student") val student: StudentDto? = null,
    @Json(name = "id") val id: Long? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "check_in_at") val checkInAt: String? = null,
    @Json(name = "check_out_at") val checkOutAt: String? = null,
    @Json(name = "check_in_method") val checkInMethod: String? = null,
    @Json(name = "check_out_method") val checkOutMethod: String? = null,
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class ParentAttendanceHistoryDto(
    @Json(name = "items") val items: List<ParentStudentAttendanceDto> = emptyList(),
    @Json(name = "page") val page: Int = 1,
    @Json(name = "page_size") val pageSize: Int = 20,
    @Json(name = "total") val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class MonthlyReportDto(
    @Json(name = "month_year") val monthYear: String,
    @Json(name = "present_count") val presentCount: Int,
    @Json(name = "late_count") val lateCount: Int,
    @Json(name = "permission_count") val permissionCount: Int,
    @Json(name = "unexcused_count") val unexcusedCount: Int,
    @Json(name = "records") val records: List<ParentStudentAttendanceDto>
)

@JsonClass(generateAdapter = true)
data class NotificationStudentDto(
    @Json(name = "id") val id: Int,
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "id") val id: Int,
    @Json(name = "type") val type: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "student") val student: NotificationStudentDto? = null,
    @Json(name = "payload") val payload: Map<String, Any?>? = emptyMap(),
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "read_at") val readAt: String? = null,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class ParentNotificationsPageDto(
    @Json(name = "items") val items: List<NotificationDto> = emptyList(),
    @Json(name = "page") val page: Int = 1,
    @Json(name = "page_size") val pageSize: Int = 20,
    @Json(name = "total") val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class NotificationUnreadCountDto(
    @Json(name = "count") val count: Int
)

@JsonClass(generateAdapter = true)
data class NotificationReadAllDto(
    @Json(name = "updated") val updated: Int
)
