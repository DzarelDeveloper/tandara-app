package id.tandara.parent.data.remote

import id.tandara.parent.data.remote.dto.ApiEnvelope
import id.tandara.parent.data.remote.dto.CreateLeaveRequestDto
import id.tandara.parent.data.remote.dto.LeaveCreateResultDto
import id.tandara.parent.data.remote.dto.LeaveResponseDto
import id.tandara.parent.data.remote.dto.UploadAttachmentResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PermissionApiService {
    @GET("api/parent/leave-requests")
    suspend fun getLeaveHistory(): Response<ApiEnvelope<List<LeaveResponseDto>>>

    @POST("api/parent/leave-requests")
    suspend fun submitLeaveRequest(
        @Body request: CreateLeaveRequestDto
    ): Response<ApiEnvelope<LeaveCreateResultDto>>

    @Multipart
    @POST("api/parent/leave-requests/attachments")
    suspend fun uploadAttachment(
        @Part file: MultipartBody.Part
    ): Response<UploadAttachmentResponseDto>
}
