package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.remote.AuthApiService
import id.tandara.parent.data.remote.dto.LoginRequestDto
import id.tandara.parent.data.remote.dto.ParentSessionDto
import id.tandara.parent.data.session.SessionManager
import id.tandara.parent.domain.model.Parent
import id.tandara.parent.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepositoryImpl(private val sessionManager: SessionManager, private val api: AuthApiService) : AuthRepository {
    override val currentParentSession: Flow<Parent?> = sessionManager.sessionFlow.map { user ->
        if (user.isAuthenticated) Parent(user.username, user.displayName, user.phoneNumber, user.role) else null
    }

    override suspend fun login(username: String, password: String): ApiResult<Parent> {
        if (username.isBlank() || password.isBlank()) return ApiResult.Error("Username dan kata sandi wajib diisi.")
        return try {
            val health = api.health()
            if (!health.isSuccessful || health.body()?.data?.status != "ok") return ApiResult.Error("Respons server Tandara tidak valid.", 502)
            val response = api.login(LoginRequestDto(username.trim(), password))
            if (!response.isSuccessful) return mapError(response.code(), response.errorBody()?.string())
            val login = response.body()?.data ?: return ApiResult.Error("Respons server Tandara tidak valid.", 502)
            if (!login.user.isActive) return ApiResult.Error("Akun tidak aktif. Hubungi Admin IT sekolah.", 403)
            if (login.user.role != "PARENT") return ApiResult.Error("Akun ini bukan akun orang tua/wali.", 403)
            val verified = api.parentSession("Bearer ${login.accessToken}")
            if (!verified.isSuccessful) return mapError(verified.code(), verified.errorBody()?.string())
            saveVerifiedSession(login.accessToken, verified.body()?.data ?: return ApiResult.Error("Respons server Tandara tidak valid.", 502))
        } catch (_: SocketTimeoutException) { ApiResult.BackendUnavailable("Waktu koneksi ke server Tandara habis.")
        } catch (_: UnknownHostException) { ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: IOException) { ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (_: Exception) { ApiResult.Error("Terjadi kesalahan saat memproses respons server.") }
    }

    override suspend fun validateSession(): ApiResult<Parent> {
        val token = sessionManager.getAccessToken() ?: return ApiResult.Error("Sesi tidak tersedia.", 401)
        return try {
            val response = api.parentSession("Bearer $token")
            if (!response.isSuccessful) {
                val result = mapError(response.code(), response.errorBody()?.string())
                if (response.code() == 401 || response.code() == 403) sessionManager.clearSession()
                result
            } else saveVerifiedSession(token, response.body()?.data ?: return ApiResult.Error("Respons server Tandara tidak valid.", 502))
        } catch (_: SocketTimeoutException) { ApiResult.BackendUnavailable("Waktu koneksi ke server Tandara habis.")
        } catch (_: IOException) { ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.") }
    }

    private suspend fun saveVerifiedSession(token: String, session: ParentSessionDto): ApiResult<Parent> {
        val parent = session.parent
        if (parent.role != "PARENT") return ApiResult.Error("Akun ini bukan akun orang tua/wali.", 403)
        val student = session.student
        sessionManager.saveSession(token, parent.fullName, parent.phoneNumber, parent.username, parent.role, student.id.toString(), student.fullName, student.nis, student.className)
        return ApiResult.Success(Parent(parent.id.toString(), parent.fullName, parent.phoneNumber, parent.role))
    }

    private fun mapError(status: Int, raw: String?): ApiResult.Error {
        val code = runCatching { JSONObject(raw.orEmpty()).optString("code") }.getOrNull()
        val message = when (code) {
            "INVALID_CREDENTIALS" -> "Username atau kata sandi tidak sesuai."
            "ACCOUNT_INACTIVE", "PARENT_PROFILE_NOT_FOUND" -> "Akun orang tua tidak aktif. Hubungi Admin IT sekolah."
            "FORBIDDEN" -> "Akun ini bukan akun orang tua/wali."
            "NO_ASSIGNED_STUDENT" -> "Akun belum memiliki siswa yang ditugaskan. Hubungi sekolah."
            "MULTIPLE_STUDENT_CONFIGURATION" -> "Konfigurasi akun tidak valid: lebih dari satu siswa terhubung."
            "UNAUTHORIZED" -> "Sesi tidak valid atau telah berakhir."
            else -> if (status >= 500) "Server Tandara sedang bermasalah." else "Permintaan tidak dapat diproses."
        }
        return ApiResult.Error(message, status)
    }

    override suspend fun logout() = sessionManager.clearSession()
    override suspend fun changePassword(current: String, new: String): ApiResult<Unit> = ApiResult.BackendUnavailable("Perubahan kata sandi belum tersedia pada fase ini.")
}
