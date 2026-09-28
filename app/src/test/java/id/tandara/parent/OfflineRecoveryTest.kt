package id.tandara.parent

import android.content.Context
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.core.network.NetworkMonitor
import id.tandara.parent.data.local.CacheDao
import id.tandara.parent.data.local.CacheEntry
import id.tandara.parent.data.local.LocalCacheStore
import id.tandara.parent.data.realtime.ParentRealtimeCoordinator
import id.tandara.parent.data.remote.AttendanceApiService
import id.tandara.parent.data.remote.AuthApiService
import id.tandara.parent.data.remote.dto.ApiEnvelope
import id.tandara.parent.data.remote.dto.ChangePasswordRequestDto
import id.tandara.parent.data.remote.dto.HealthDto
import id.tandara.parent.data.remote.dto.LoginRequestDto
import id.tandara.parent.data.remote.dto.LoginResponseDto
import id.tandara.parent.data.remote.dto.ParentAttendanceHistoryDto
import id.tandara.parent.data.remote.dto.ParentSessionDto
import id.tandara.parent.data.remote.dto.ParentStudentAttendanceDto
import id.tandara.parent.data.repository.AttendanceRepositoryImpl
import id.tandara.parent.data.repository.AuthRepositoryImpl
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
        override suspend fun saveSession(accessToken: String, parentId: String, displayName: String, phoneNumber: String, username: String, role: String, studentId: String, studentName: String, studentNis: String, studentClass: String) {
            token = accessToken
            state.value = SessionUser(true, displayName, phoneNumber, username, role, parentId, studentId, studentName, studentNis, studentClass)
            cleared = false
        }
        override suspend fun clearSession() {
            token = null
            state.value = SessionUser()
            cleared = true
        }
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

    private class TestParentRepository : ParentRepository {
        override suspend fun getAssignedStudent() = ApiResult.Success(Student("student-4", "nis-4", "Alya", "7A"))
        override suspend fun getLinkedStudents() = ApiResult.Success(listOf(Student("student-4", "nis-4", "Alya", "7A")))
        override suspend fun getNotifications() = ApiResult.Success(emptyList<ParentNotification>())
        override suspend fun getUnreadNotificationCount() = ApiResult.Success(0)
        override suspend fun markNotificationRead(notificationId: String): ApiResult<ParentNotification> = error("Not used")
        override suspend fun markAllNotificationsRead(): ApiResult<Int> = error("Not used")
        override suspend fun cacheRealtimeNotification(notification: ParentNotification) = Unit
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
        val repository = AuthRepositoryImpl(session, FailingAuthApi(), LocalCacheStore(MemoryDao()))

        val result = repository.validateSession()

        assertTrue(result is ApiResult.BackendUnavailable)
        assertTrue(session.sessionFlow.first().isAuthenticated)
        assertEquals("test-token-never-logged", session.getAccessToken())
        assertFalse(session.cleared)

        apiResponsePreservesSession(session)
    }

    private suspend fun apiResponsePreservesSession(session: TestSessionStore) {
        val api = FailingAuthApi().apply {
            sessionResponse = Response.error(503, "{}".toResponseBody())
        }
        val result = AuthRepositoryImpl(session, api, LocalCacheStore(MemoryDao())).validateSession()
        assertTrue(result is ApiResult.BackendUnavailable)
        assertTrue(session.sessionFlow.first().isAuthenticated)
        assertFalse(session.cleared)
    }

    @Test
    fun actualUnauthorizedResponseClearsParentSession() = runTest {
        val session = TestSessionStore()
        val api = FailingAuthApi().apply {
            sessionResponse = Response.error(401, "{}".toResponseBody())
        }
        val repository = AuthRepositoryImpl(session, api, LocalCacheStore(MemoryDao()))

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
        val api = TestAttendanceApi()
        val repository = AttendanceRepositoryImpl(api, session, cache)
        val month = LocalDate.now()
        val period = "%04d-%02d".format(month.year, month.monthValue)
        val stale = AttendanceRecord("old-1", "01 Sep 2026", status = AttendanceStatus.PRESENT)
        cache.putReports("parent-12", "student-4", period, listOf(stale))

        api.historyFailure = IOException("backend unavailable")
        val offline = repository.getMonthlyAttendanceRecords("student-4", month.monthValue, month.year)
        assertTrue(offline is ApiResult.Success && offline.isStale)
        assertEquals(listOf(stale), (offline as ApiResult.Success).data)

        val current = ParentStudentAttendanceDto(
            id = 22L,
            date = month.withDayOfMonth(2).toString(),
            status = "LATE"
        )
        api.historyFailure = null
        api.history = Response.success(
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
        val api = TestAttendanceApi()
        val repository = AttendanceRepositoryImpl(api, session, cache)
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
            val coordinator = ParentRealtimeCoordinator(
                session,
                AuthRepositoryImpl(session, FailingAuthApi(), LocalCacheStore(MemoryDao())),
                parentRepository,
                NetworkMonitor(context)
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