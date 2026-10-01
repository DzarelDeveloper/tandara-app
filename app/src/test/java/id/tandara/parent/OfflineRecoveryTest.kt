package id.tandara.parent

import android.content.Context
import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import id.tandara.parent.core.common.SystemNotificationManager
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.core.network.NetworkConfigManager
import id.tandara.parent.core.network.NetworkMonitor
import id.tandara.parent.core.network.ServerConfig
import id.tandara.parent.data.local.CacheDao
import id.tandara.parent.data.local.CacheEntry
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.realtime.ParentRealtimeCoordinator
import id.tandara.parent.data.remote.ApiClient
import id.tandara.parent.data.remote.AttendanceApiService
import id.tandara.parent.data.remote.AuthApiService
import id.tandara.parent.data.remote.ParentApiService
import id.tandara.parent.data.remote.PermissionApiService
import id.tandara.parent.data.remote.dto.ApiEnvelope
import id.tandara.parent.data.remote.dto.ChangePasswordRequestDto
import id.tandara.parent.data.remote.dto.HealthDto
import id.tandara.parent.data.remote.dto.LeaveCreateResultDto
import id.tandara.parent.data.remote.dto.LeaveResponseDto
import id.tandara.parent.data.remote.dto.LoginRequestDto
import id.tandara.parent.data.remote.dto.LoginResponseDto
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.remote.dto.NotificationReadAllDto
import id.tandara.parent.data.remote.dto.NotificationUnreadCountDto
import id.tandara.parent.data.remote.dto.ParentAttendanceHistoryDto
import id.tandara.parent.data.remote.dto.ParentNotificationsPageDto
import id.tandara.parent.data.remote.dto.ParentProfileDto
import id.tandara.parent.data.remote.dto.ParentSessionDto
import id.tandara.parent.data.remote.dto.ParentStudentAttendanceDto
import id.tandara.parent.data.remote.dto.ProfilePhotoDto
import id.tandara.parent.data.remote.dto.StudentDto
import id.tandara.parent.data.remote.dto.UploadAttachmentResponseDto
import id.tandara.parent.data.repository.AttendanceRepositoryImpl
import id.tandara.parent.data.repository.AuthRepositoryImpl
import id.tandara.parent.data.server.ServerConfigStore
import id.tandara.parent.data.session.SessionStore
import id.tandara.parent.data.session.SessionUser
import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.domain.model.AttendanceSummary
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.model.Student
import id.tandara.parent.domain.repository.AttendanceRepository
import id.tandara.parent.domain.repository.ParentRepository
import id.tandara.parent.ui.reports.ReportsViewModel
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = OfflineRecoveryTest.TestApplication::class)
class OfflineRecoveryTest {
    class TestApplication : Application()

    private class MemoryDao : CacheDao {
        private val entries = linkedMapOf<String, CacheEntry>()
        override suspend fun get(id: String) = entries[id]
        override suspend fun put(entry: CacheEntry) { entries[entry.cacheId] = entry }
        override suspend fun delete(id: String) { entries.remove(id) }
        override suspend fun clearAccount(accountId: String) {
            entries.entries.removeAll { it.value.accountId == accountId }
        }
    }

    private class TestSessionStore : SessionStore {
        private val state = MutableStateFlow(
            SessionUser(
                isAuthenticated = true,
                displayName = "Parent",
                phoneNumber = "081234567890",
                username = "parent",
                role = "PARENT",
                parentId = "parent-12",
                studentId = "student-4",
                studentName = "Alya",
                studentNis = "nis-4",
                studentClass = "7A"
            )
        )
        override val sessionFlow: Flow<SessionUser> = state
        var token: String? = "test-token-never-logged"
        var cleared = false
        override fun getAccessToken() = token
        override suspend fun saveSession(accessToken: String, parentId: String, displayName: String, phoneNumber: String, username: String, role: String, studentId: String, studentName: String, studentNis: String, studentClass: String, parentPhotoUrl: String, studentPhotoUrl: String) {
            token = accessToken
            state.value = SessionUser(true, displayName, phoneNumber, username, role, parentId, studentId, studentName, studentNis, studentClass, parentPhotoUrl, studentPhotoUrl)
            cleared = false
        }
        override suspend fun clearSession() {
            token = null
            state.value = SessionUser()
            cleared = true
        }
    }

    private class MemoryServerConfigStore(
        initial: ServerConfig = ServerConfig("127.0.0.1", 8000)
    ) : ServerConfigStore {
        private val stored = MutableStateFlow(initial)
        override val configFlow: Flow<ServerConfig> = stored
        override suspend fun getCurrent(): ServerConfig = stored.value
        override suspend fun save(config: ServerConfig) { stored.value = config }
    }

    private fun testNetworkConfigManager(): NetworkConfigManager {
        return NetworkConfigManager(MemoryServerConfigStore())
    }

    private class FailingAuthApi : AuthApiService {
        var sessionResponse: Response<ApiEnvelope<ParentSessionDto>>? = null
        override suspend fun health(): Response<ApiEnvelope<HealthDto>> = error("Not used")
        override suspend fun login(request: LoginRequestDto): Response<ApiEnvelope<LoginResponseDto>> = error("Not used")
        override suspend fun parentSession(authorization: String): Response<ApiEnvelope<ParentSessionDto>> {
            sessionResponse?.let { return it }
            throw IOException("temporary network failure")
        }
        override suspend fun changePassword(request: ChangePasswordRequestDto): Response<Unit> = error("Not used")
    }

    private class TestAttendanceApi : AttendanceApiService {
        var today: Response<ApiEnvelope<ParentStudentAttendanceDto>> = Response.success(
            ApiEnvelope(success = true, data = ParentStudentAttendanceDto(id = null))
        )
        var history: Response<ApiEnvelope<ParentAttendanceHistoryDto>> = Response.success(
            ApiEnvelope(success = true, data = ParentAttendanceHistoryDto())
        )
        var historyFailure: IOException? = null

        override suspend fun getTodayAttendance(studentId: String) = today

        override suspend fun getHistory(
            studentId: String,
            dateFrom: String?,
            dateTo: String?,
            status: String?,
            page: Int,
            pageSize: Int
        ): Response<ApiEnvelope<ParentAttendanceHistoryDto>> {
            historyFailure?.let { throw it }
            return history
        }
    }

    private class FakeParentApi : ParentApiService {
        override suspend fun updateProfilePhoto(image: MultipartBody.Part): Response<ApiEnvelope<ProfilePhotoDto>> = error("Not used")
        override suspend fun getProfile(): Response<ParentProfileDto> = error("Not used")
        override suspend fun getLinkedStudents(): Response<List<StudentDto>> = error("Not used")
        override suspend fun getNotifications(
            page: Int,
            pageSize: Int,
            unreadOnly: Boolean
        ): Response<ApiEnvelope<ParentNotificationsPageDto>> = error("Not used")
        override suspend fun getUnreadNotificationCount(): Response<ApiEnvelope<NotificationUnreadCountDto>> = error("Not used")
        override suspend fun markNotificationRead(notificationId: Int): Response<ApiEnvelope<NotificationDto>> = error("Not used")
        override suspend fun markAllNotificationsRead(): Response<ApiEnvelope<NotificationReadAllDto>> = error("Not used")
    }

    private class FakePermissionApi : PermissionApiService {
        override suspend fun getLeaveHistory(): Response<ApiEnvelope<List<LeaveResponseDto>>> = error("Not used")
        override suspend fun submitLeaveRequest(
            request: id.tandara.parent.data.remote.dto.CreateLeaveRequestDto
        ): Response<ApiEnvelope<LeaveCreateResultDto>> = error("Not used")
        override suspend fun uploadAttachment(file: MultipartBody.Part): Response<UploadAttachmentResponseDto> = error("Not used")
    }

    private fun authApiClient(session: SessionStore, authApi: AuthApiService): ApiClient {
        val ncm = testNetworkConfigManager()
        return object : ApiClient(session, ncm) {
            override val authApiService: AuthApiService = authApi
            override val parentApiService: ParentApiService = FakeParentApi()
            override val attendanceApiService: AttendanceApiService = TestAttendanceApi()
            override val permissionApiService: PermissionApiService = FakePermissionApi()
        }
    }

    private fun attendanceApiClient(session: SessionStore, attendanceApi: AttendanceApiService): ApiClient {
        val ncm = testNetworkConfigManager()
        return object : ApiClient(session, ncm) {
            override val authApiService: AuthApiService = FailingAuthApi()
            override val parentApiService: ParentApiService = FakeParentApi()
            override val attendanceApiService: AttendanceApiService = attendanceApi
            override val permissionApiService: PermissionApiService = FakePermissionApi()
        }
    }

    private class TestParentRepository : ParentRepository {
        override suspend fun getAssignedStudent() = ApiResult.Success(Student("student-4", "nis-4", "Alya", "7A"))
        override suspend fun getLinkedStudents() = ApiResult.Success(listOf(Student("student-4", "nis-4", "Alya", "7A")))
        override suspend fun getNotifications() = ApiResult.Success(emptyList<ParentNotification>())
        override suspend fun getUnreadNotificationCount() = ApiResult.Success(0)
        override suspend fun markNotificationRead(notificationId: String): ApiResult<ParentNotification> = error("Not used")
        override suspend fun markAllNotificationsRead(): ApiResult<Int> = error("Not used")
        override suspend fun cacheRealtimeNotification(notification: ParentNotification) = Unit
        override suspend fun updateParentPhoto(uri: Uri): ApiResult<String> = error("Not used")
    }

    private class TestAttendanceRepository(
        private val result: ApiResult<List<AttendanceRecord>>
    ) : AttendanceRepository {
        override suspend fun getTodayAttendance(studentId: String): ApiResult<AttendanceRecord?> = error("Not used")
        override suspend fun getMonthlyAttendanceSummary(studentId: String, month: Int, year: Int): ApiResult<AttendanceSummary> = error("Not used")
        override suspend fun getMonthlyAttendanceRecords(studentId: String, month: Int, year: Int) = result
    }

    @Test
    fun temporaryValidationFailurePreservesValidParentSession() = runTest {
        val session = TestSessionStore()
        val repository = AuthRepositoryImpl(session, authApiClient(session, FailingAuthApi()), LocalCacheStore(MemoryDao()))

        val result = repository.validateSession()

        assertTrue(result is ApiResult.BackendUnavailable)
        assertTrue(session.sessionFlow.first().isAuthenticated)
        assertEquals("test-token-never-logged", session.getAccessToken())
        assertFalse(session.cleared)

        apiResponsePreservesSession(session)
    }

    private suspend fun apiResponsePreservesSession(session: TestSessionStore) {
        val failingApi = FailingAuthApi().apply {
            sessionResponse = Response.error(503, "{}".toResponseBody())
        }
        val result = AuthRepositoryImpl(session, authApiClient(session, failingApi), LocalCacheStore(MemoryDao())).validateSession()
        assertTrue(result is ApiResult.BackendUnavailable)
        assertTrue(session.sessionFlow.first().isAuthenticated)
        assertFalse(session.cleared)
    }

    @Test
    fun actualUnauthorizedResponseClearsParentSession() = runTest {
        val session = TestSessionStore()
        val failingApi = FailingAuthApi().apply {
            sessionResponse = Response.error(401, "{}".toResponseBody())
        }
        val repository = AuthRepositoryImpl(session, authApiClient(session, failingApi), LocalCacheStore(MemoryDao()))

        val result = repository.validateSession()

        assertTrue(result is ApiResult.Error && result.code == 401)
        assertFalse(session.sessionFlow.first().isAuthenticated)
        assertTrue(session.cleared)
    }

    @Test
    fun offlineReportsAreStaleThenAuthoritativeRefreshReplacesThem() = runTest {
        val session = TestSessionStore()
        val dao = MemoryDao()
        val cache = LocalCacheStore(dao)
        val attendanceApi = TestAttendanceApi()
        val repository = AttendanceRepositoryImpl(attendanceApiClient(session, attendanceApi), session, cache)
        val month = LocalDate.now()
        val period = "%04d-%02d".format(month.year, month.monthValue)
        val stale = AttendanceRecord("old-1", "01 Sep 2026", status = AttendanceStatus.PRESENT)
        cache.putReports("parent-12", "student-4", period, listOf(stale))

        attendanceApi.historyFailure = IOException("backend unavailable")
        val offline = repository.getMonthlyAttendanceRecords("student-4", month.monthValue, month.year)
        assertTrue(offline is ApiResult.Success && offline.isStale)
        assertEquals(listOf(stale), (offline as ApiResult.Success).data)

        val current = ParentStudentAttendanceDto(
            id = 22L,
            date = month.withDayOfMonth(2).toString(),
            status = "LATE"
        )
        attendanceApi.historyFailure = null
        attendanceApi.history = Response.success(
            ApiEnvelope(success = true, data = ParentAttendanceHistoryDto(items = listOf(current, current)))
        )
        repeat(2) {
            val refreshed = repository.getMonthlyAttendanceRecords("student-4", month.monthValue, month.year)
            assertTrue(refreshed is ApiResult.Success && !refreshed.isStale)
            assertEquals(1, (refreshed as ApiResult.Success).data.size)
        }

        val cached = cache.reports("parent-12", "student-4", period)?.value.orEmpty()
        assertEquals(1, cached.size)
        assertEquals("22", cached.single().id)
        assertEquals(AttendanceStatus.LATE, cached.single().status)
    }

    @Test
    fun successfulEmptyTodayClearsCacheAndYesterdayIsNotToday() = runTest {
        val session = TestSessionStore()
        val cache = LocalCacheStore(MemoryDao())
        val attendanceApi = TestAttendanceApi()
        val repository = AttendanceRepositoryImpl(attendanceApiClient(session, attendanceApi), session, cache)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val yesterdayRecord = AttendanceRecord("yesterday", "${yesterday}", status = AttendanceStatus.PRESENT)
        cache.putToday("parent-12", "student-4", yesterday.toString(), yesterdayRecord)
        cache.putToday("parent-12", "student-4", today.toString(), yesterdayRecord)

        val result = repository.getTodayAttendance("student-4")

        assertTrue(result is ApiResult.Success)
        assertNull((result as ApiResult.Success).data)
        assertNull(cache.today("parent-12", "student-4", today.toString()))
        assertEquals(yesterdayRecord, cache.today("parent-12", "student-4", yesterday.toString())?.value)
        assertNull(AttendanceRecord("", today.toString(), status = AttendanceStatus.UNKNOWN).punctualityLabel)
    }

    @Test
    fun reportsViewModelMarksCachedResultsOffline() = runTest {
        val previousMain = Dispatchers.Main
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val session = TestSessionStore()
            val parentRepository = TestParentRepository()
            val record = AttendanceRecord("cached-1", "29 Sep 2026", status = AttendanceStatus.PRESENT)
            val reports = TestAttendanceRepository(ApiResult.Success(listOf(record), isStale = true, lastUpdatedAt = 1234L))
            val ncm = testNetworkConfigManager()
            val authApi = authApiClient(session, FailingAuthApi())
            val authRepository = AuthRepositoryImpl(session, authApi, LocalCacheStore(MemoryDao()))
            val coordinator = ParentRealtimeCoordinator(
                session,
                authRepository,
                parentRepository,
                NetworkMonitor(context),
                ncm,
                SystemNotificationManager(context)
            )
            val viewModel = ReportsViewModel(parentRepository, reports, coordinator)

            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isOffline)
            assertEquals(listOf(record), viewModel.uiState.value.records)
            assertEquals(1234L, viewModel.uiState.value.lastUpdatedAt)
        } finally {
            Dispatchers.resetMain()
        }
    }
}