package id.tandara.parent.data.remote

import id.tandara.parent.data.remote.dto.ChangePasswordRequestDto
import id.tandara.parent.data.remote.dto.LoginRequestDto
import id.tandara.parent.data.remote.dto.LoginResponseDto
import id.tandara.parent.data.remote.dto.ApiEnvelope
import id.tandara.parent.data.remote.dto.HealthDto
import id.tandara.parent.data.remote.dto.ParentSessionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @retrofit2.http.GET("api/health")
    suspend fun health(): Response<ApiEnvelope<HealthDto>>
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<ApiEnvelope<LoginResponseDto>>
    @retrofit2.http.GET("api/parent/session")
    suspend fun parentSession(@retrofit2.http.Header("Authorization") authorization: String): Response<ApiEnvelope<ParentSessionDto>>

    @POST("api/parent/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequestDto
    ): Response<Unit>
}
