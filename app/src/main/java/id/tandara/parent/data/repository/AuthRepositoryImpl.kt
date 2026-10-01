package id.tandara.parent.data.repository

import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.core.network.NetworkDiagnostics
import id.tandara.parent.data.remote.AuthApiService
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.remote.dto.LoginRequestDto
import id.tandara.parent.data.remote.dto.ParentSessionDto
import id.tandara.parent.data.session.SessionStore
import id.tandara.parent.domain.model.Parent
import id.tandara.parent.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepositoryImpl(private val sessionManager: SessionStore, private val api: AuthApiService, private val cache: LocalCacheStore) : AuthRepository {
    override val currentParentSession: Flow<Parent?> = sessionManager.sessionFlow.map { user ->
        if (user.isAuthenticated) Parent(user.parentId, user.displayName, user.phoneNumber, user.role) else null
    }

    override suspend fun login(username: String, password: String): ApiResult<Parent> {
        if (username.isBlank() || password.isBlank()) return ApiResult.Error("Username dan kata sandi wajib diisi.")
        return try {
            val health = api.health()
            if (!health.isSuccessful || health.body()?.data?.status != "ok") {
                if (health.isSuccessful) NetworkDiagnostics.logInvalidServerResponse("GET /api/health")
                return ApiResult.Error("Respons server Tandara tidak valid.", 502)
            }
            val response = api.login(LoginRequestDto(username.trim(), password))
            if (!response.isSuccessful) return mapError(response.code(), response.errorBody()?.string())
            val login = response.body()?.data ?: run {
                NetworkDiagnostics.logInvalidServerResponse("POST /api/auth/login")
                return ApiResult.Error("Respons server Tandara tidak valid.", 502)
            }
            if (!login.user.isActive) return ApiResult.Error("Akun tidak aktif. Hubungi Admin IT sekolah.", 403)
            if (login.user.role != "PARENT") return ApiResult.Error("Akun ini bukan akun orang tua/wali.", 403)
            val verified = api.parentSession("Bearer ${login.accessToken}")
            if (!verified.isSuccessful) return mapError(verified.code(), verified.errorBody()?.string())
            val session = verified.body()?.data ?: run {
                NetworkDiagnostics.logInvalidServerResponse("GET /api/parent/session")
                return ApiResult.Error("Respons server Tandara tidak valid.", 502)
            }
            saveVerifiedSession(login.accessToken, session)
        } catch (error: SocketTimeoutException) {
            NetworkDiagnostics.logFailure("POST /api/auth/login", error)
            ApiResult.BackendUnavailable("Waktu koneksi ke server Tandara habis.")
        } catch (error: UnknownHostException) {
            NetworkDiagnostics.logFailure("POST /api/auth/login", error)
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("POST /api/auth/login", error)
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("POST /api/auth/login", error)
            ApiResult.Error("Terjadi kesalahan saat memproses respons server.")
        }
    }

    override suspend fun validateSession(): ApiResult<Parent> {
        val token = sessionManager.getAccessToken() ?: return ApiResult.Error("Sesi tidak tersedia.", 401)
        return try {
            val response = api.parentSession("Bearer $token")
            if (!response.isSuccessful) {
                val code = response.code()
                if (code == 401 || code == 403) {
                    sessionManager.clearSession()
                    return mapError(code, response.errorBody()?.string())
                }
                if (code == 408 || code == 429 || code in 500..599) {
                    response.errorBody()?.close()
                    return ApiResult.BackendUnavailable("Server Tandara sedang tidak tersedia.")
                }
                mapError(code, response.errorBody()?.string())
            } else {
                val session = response.body()?.data ?: run {
                    NetworkDiagnostics.logInvalidServerResponse("GET /api/parent/session")
                    return ApiResult.Error("Respons server Tandara tidak valid.", 502)
                }
                saveVerifiedSession(token, session)
            }
        } catch (error: SocketTimeoutException) {
            NetworkDiagnostics.logFailure("GET /api/parent/session", error)
            ApiResult.BackendUnavailable("Waktu koneksi ke server Tandara habis.")
        } catch (error: IOException) {
            NetworkDiagnostics.logFailure("GET /api/parent/session", error)
            ApiResult.BackendUnavailable("Tidak dapat terhubung ke server Tandara.")
        } catch (error: Exception) {
            NetworkDiagnostics.logFailure("GET /api/parent/session", error)
            ApiResult.Error("Respons server Tandara tidak valid.", 502)
        }
    }

    private suspend fun saveVerifiedSession(token: String, session: ParentSessionDto): ApiResult<Parent> {
        val parent = session.parent
        if (parent.role != "PARENT") return ApiResult.Error("Akun ini bukan akun orang tua/wali.", 403)
        val student = session.student
        sessionManager.saveSession(token, parent.id.toString(), parent.fullName, parent.phoneNumber, parent.username, parent.role, student.id.toString(), student.fullName, student.nis, student.className)
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

    override suspend fun logout() {
        val session = sessionManager.sessionFlow.first()
        val account = session.parentId.ifBlank { session.username }
        if (account.isNotBlank()) cache.clearAccount(account)
        sessionManager.clearSession()
    }
    override suspend fun changePassword(current: String, new: String): ApiResult<Unit> = ApiResult.BackendUnavailable("Perubahan kata sandi belum tersedia pada fase ini.")
}
