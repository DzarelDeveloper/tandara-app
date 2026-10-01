package id.tandara.parent.data.remote

import id.tandara.parent.data.remote.dto.ApiEnvelope
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.remote.dto.ParentNotificationsPageDto
import id.tandara.parent.data.remote.dto.NotificationReadAllDto
import id.tandara.parent.data.remote.dto.NotificationUnreadCountDto
import id.tandara.parent.data.remote.dto.ParentProfileDto
import id.tandara.parent.data.remote.dto.StudentDto
import id.tandara.parent.data.remote.dto.ProfilePhotoDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ParentApiService {
    @Multipart
    @POST("api/parent/profile/photo")
    suspend fun updateProfilePhoto(@Part image: MultipartBody.Part): Response<ApiEnvelope<ProfilePhotoDto>>
    @GET("api/v1/parent/profile")
    suspend fun getProfile(): Response<ParentProfileDto>

    @GET("api/v1/parent/students")
    suspend fun getLinkedStudents(): Response<List<StudentDto>>

    @GET("api/parent/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("unread_only") unreadOnly: Boolean = false
    ): Response<ApiEnvelope<ParentNotificationsPageDto>>

    @GET("api/parent/notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<ApiEnvelope<NotificationUnreadCountDto>>

    @PATCH("api/parent/notifications/{notification_id}/read")
    suspend fun markNotificationRead(
        @Path("notification_id") notificationId: Int
    ): Response<ApiEnvelope<NotificationDto>>

    @PATCH("api/parent/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiEnvelope<NotificationReadAllDto>>
}
