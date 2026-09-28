package id.tandara.parent.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateLeaveRequestDto(
    @Json(name = "student_id") val studentId: Int,
    @Json(name = "leave_date") val leaveDate: String,
    @Json(name = "leave_type") val leaveType: String,
    @Json(name = "reason") val reason: String
)

@JsonClass(generateAdapter = true)
data class LeaveResponseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "student_id") val studentId: Long? = null,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "leave_date") val leaveDate: String? = null,
    @Json(name = "leave_type") val leaveType: String? = null,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaveCreateResultDto(
    @Json(name = "id") val id: Long,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class UploadAttachmentResponseDto(
    @Json(name = "attachment_id") val attachmentId: String,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_url") val fileUrl: String
)
