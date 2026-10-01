package id.tandara.parent.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(val success: Boolean, val data: T? = null, val message: String? = null)
@JsonClass(generateAdapter = true)
data class LoginRequestDto(val username: String, val password: String)
@JsonClass(generateAdapter = true)
data class LoginResponseDto(@Json(name = "access_token") val accessToken: String, @Json(name = "token_type") val tokenType: String, val user: AuthUserDto)
@JsonClass(generateAdapter = true)
data class AuthUserDto(val id: String, val username: String, @Json(name = "displayName") val displayName: String, val role: String, @Json(name = "isActive") val isActive: Boolean)
@JsonClass(generateAdapter = true)
data class ParentSessionDto(val parent: ParentSessionParentDto, val student: ParentSessionStudentDto)
@JsonClass(generateAdapter = true)
data class ParentSessionParentDto(val id: Long, @Json(name = "full_name") val fullName: String, val username: String, val role: String, @Json(name = "phone_number") val phoneNumber: String, @Json(name = "is_active") val isActive: Boolean, @Json(name = "photo_url") val photoUrl: String? = null)
@JsonClass(generateAdapter = true)
data class ParentSessionStudentDto(val id: Long, val nis: String, @Json(name = "full_name") val fullName: String, @Json(name = "class") val className: String, val grade: String, val major: String, @Json(name = "photo_url") val photoUrl: String? = null)
@JsonClass(generateAdapter = true)
data class ProfilePhotoDto(@Json(name = "photo_url") val photoUrl: String)
@JsonClass(generateAdapter = true)
data class HealthDto(val status: String)
@JsonClass(generateAdapter = true)
data class ChangePasswordRequestDto(@Json(name = "current_password") val currentPassword: String, @Json(name = "new_password") val newPassword: String)
@JsonClass(generateAdapter = true)
data class ParentProfileDto(val id: String, val name: String, @Json(name = "phone_number") val phoneNumber: String, val role: String)
