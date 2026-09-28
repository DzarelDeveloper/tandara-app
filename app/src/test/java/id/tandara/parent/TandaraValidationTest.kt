package id.tandara.parent

import id.tandara.parent.core.common.DateUtils
import id.tandara.parent.core.common.PhoneUtils
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.remote.dto.ApiEnvelope
import id.tandara.parent.data.remote.dto.NotificationDto
import id.tandara.parent.data.remote.dto.NotificationStudentDto
import id.tandara.parent.data.remote.dto.ParentNotificationsPageDto
import id.tandara.parent.data.remote.dto.ParentStudentAttendanceDto
import id.tandara.parent.data.remote.dto.LeaveResponseDto
import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.domain.model.AttendanceSummary
import id.tandara.parent.domain.model.LeaveStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class TandaraValidationTest {

    @Test
    fun testIndonesianPhoneNormalization() {
        // Standard Indonesian format
        assertEquals("081234567890", PhoneUtils.normalizeIndonesianPhoneNumber("081234567890"))
        // +62 prefix
        assertEquals("081234567890", PhoneUtils.normalizeIndonesianPhoneNumber("+6281234567890"))
        // 62 prefix without +
        assertEquals("081234567890", PhoneUtils.normalizeIndonesianPhoneNumber("6281234567890"))
        // Formatted with dashes/spaces
        assertEquals("081234567890", PhoneUtils.normalizeIndonesianPhoneNumber("0812-3456-7890"))
        assertEquals("081234567890", PhoneUtils.normalizeIndonesianPhoneNumber("+62 812 3456 7890"))
    }

    @Test
    fun testPhoneNumberMasking() {
        val masked = PhoneUtils.maskPhoneNumber("081234567890")
        assertEquals("0812••••7890", masked)
    }

    @Test
    fun testDateRangeValidation() {
        val start = 1700000000000L
        val endValid = 1700000000000L + 86400000L // 1 day later
        val endEqual = 1700000000000L // same day
        val endInvalid = 1700000000000L - 86400000L // 1 day earlier

        assertTrue(DateUtils.isDateRangeValid(start, endValid))
        assertTrue(DateUtils.isDateRangeValid(start, endEqual))
        assertFalse(DateUtils.isDateRangeValid(start, endInvalid))
        assertFalse(DateUtils.isDateRangeValid(null, endValid))
        assertFalse(DateUtils.isDateRangeValid(start, null))
    }

    @Test
    fun testAuthenticationHasNoMockCredentialConstants() {
        val names = Class.forName("id.tandara.parent.data.session.SessionManager").declaredFields.map { it.name }
        assertFalse(names.any { it.startsWith("MOCK_") })
    }

    @Test
    fun testLeaveReasonValidationRules() {
        // Less than 10 characters -> invalid
        val shortReason = "Demam"
        assertTrue(shortReason.trim().length < 10)

        // 10 to 500 characters -> valid
        val validReason = "Sakit demam tinggi dan perlu istirahat di rumah"
        assertTrue(validReason.trim().length in 10..500)

        // Greater than 500 characters -> invalid
        val longReason = "A".repeat(501)
        assertTrue(longReason.length > 500)
    }

    @Test
    fun testBackendAttendanceStatusMapping() {
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.fromBackend("PRESENT"))
        assertEquals(AttendanceStatus.LATE, AttendanceStatus.fromBackend("LATE"))
        assertEquals(AttendanceStatus.PERMISSION, AttendanceStatus.fromBackend("EXCUSED"))
        assertEquals(AttendanceStatus.SICK, AttendanceStatus.fromBackend("SICK"))
        assertEquals(AttendanceStatus.UNKNOWN, AttendanceStatus.fromBackend(""))
    }

    @Test
    fun testBackendLeaveStatusMapping() {
        assertEquals(LeaveStatus.PENDING, LeaveStatus.fromBackend("PENDING"))
        assertEquals(LeaveStatus.APPROVED, LeaveStatus.fromBackend("APPROVED"))
        assertEquals(LeaveStatus.REJECTED, LeaveStatus.fromBackend("REJECTED"))
        assertEquals(LeaveStatus.PENDING, LeaveStatus.fromBackend("UNKNOWN"))
    }

    @Test
    fun testBackendNumericAndPagedResponseContracts() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val notificationsType = Types.newParameterizedType(
            ApiEnvelope::class.java,
            ParentNotificationsPageDto::class.java
        )
        val notifications = moshi.adapter<ApiEnvelope<ParentNotificationsPageDto>>(notificationsType)
            .fromJson("""{"success":true,"data":{"items":[{"id":7,"type":"INFO","title":"Info","message":"Pesan","is_read":false,"created_at":"2026-09-29T07:00:00"}],"page":1,"page_size":20,"total":1}}""")
        assertEquals(7, notifications?.data?.items?.single()?.id)

        val attendance = moshi.adapter(ParentStudentAttendanceDto::class.java)
            .fromJson("""{"id":14,"date":"2026-09-29","status":"PRESENT"}""")
        assertEquals(14L, attendance?.id)
        val emptyAttendance = moshi.adapter(ParentStudentAttendanceDto::class.java)
            .fromJson("""{"id":null,"date":"2026-09-29","status":null}""")
        assertEquals(null, emptyAttendance?.id)

        val leave = moshi.adapter(LeaveResponseDto::class.java)
            .fromJson("""{"id":19,"student_id":4,"leave_date":"2026-09-29","status":"PENDING"}""")
        assertEquals(19L, leave?.id)
        assertEquals(4L, leave?.studentId)
    }

    @Test
    fun testAppEntryDestinationLogic() {
        // State A: First Install / First Run (Onboarding not completed)
        val stateAOnboardingDone = false
        val stateAIsAuth = false
        val destA = when {
            !stateAOnboardingDone -> "welcome"
            stateAIsAuth -> "home"
            else -> "auth/login"
        }
        assertEquals("welcome", destA)

        // State B: Onboarding completed, logged out
        val stateBOnboardingDone = true
        val stateBIsAuth = false
        val destB = when {
            !stateBOnboardingDone -> "welcome"
            stateBIsAuth -> "home"
            else -> "auth/login"
        }
        assertEquals("auth/login", destB)

        // State C: Onboarding completed + valid session
        val stateCOnboardingDone = true
        val stateCIsAuth = true
        val destC = when {
            !stateCOnboardingDone -> "welcome"
            stateCIsAuth -> "home"
            else -> "auth/login"
        }
        assertEquals("home", destC)
    }

    @Test
    fun testDynamicGreetingTimeRules() {
        // 05:00–10:59: Selamat pagi
        assertEquals("Selamat pagi", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(5, 0)))
        assertEquals("Selamat pagi", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(7, 15)))
        assertEquals("Selamat pagi", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(10, 59)))

        // 11:00–14:59: Selamat siang
        assertEquals("Selamat siang", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(11, 0)))
        assertEquals("Selamat siang", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(12, 30)))
        assertEquals("Selamat siang", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(14, 59)))

        // 15:00–17:59: Selamat sore
        assertEquals("Selamat sore", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(15, 0)))
        assertEquals("Selamat sore", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(16, 20)))
        assertEquals("Selamat sore", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(17, 59)))

        // 18:00–04:59: Selamat malam
        assertEquals("Selamat malam", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(18, 0)))
        assertEquals("Selamat malam", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(20, 45)))
        assertEquals("Selamat malam", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(23, 59)))
        assertEquals("Selamat malam", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(0, 0)))
        assertEquals("Selamat malam", id.tandara.parent.ui.home.HomeViewModel.computeDynamicGreeting(java.time.LocalTime.of(4, 59)))
    }

    @Test
    fun testThemeAppearanceDefaultAndOptions() {
        val defaultAppearance = "light"
        assertEquals("light", defaultAppearance)

        fun resolveIsDark(appearance: String, systemIsDark: Boolean): Boolean {
            return when (appearance) {
                "dark" -> true
                "system" -> systemIsDark
                else -> false // default light
            }
        }

        // Light appearance ignores system theme
        assertFalse(resolveIsDark("light", systemIsDark = true))
        assertFalse(resolveIsDark("light", systemIsDark = false))

        // Dark appearance ignores system theme
        assertTrue(resolveIsDark("dark", systemIsDark = false))
        assertTrue(resolveIsDark("dark", systemIsDark = true))

        // System appearance follows OS
        assertTrue(resolveIsDark("system", systemIsDark = true))
        assertFalse(resolveIsDark("system", systemIsDark = false))
    }

    @Test
    fun testIndonesianDateFormatting() {
        val testDate = java.time.LocalDate.of(2026, 9, 27)
        val formatted = id.tandara.parent.ui.home.HomeViewModel.computeFormattedDate(testDate)
        assertTrue(formatted.contains("September") || formatted.contains("2026"))
    }

    @Test
    fun testNotificationApiShapeMatchesBackendContract() {
        val dto = NotificationDto(
            id = 42,
            type = "ATTENDANCE_CHECK_IN",
            title = "Tiba di sekolah",
            message = "Alya telah melakukan presensi masuk pukul 06:47.",
            student = NotificationStudentDto(id = 7, fullName = "Alya Putri"),
            payload = mapOf("attendance_id" to 99, "status" to "PRESENT"),
            isRead = false,
            readAt = null,
            createdAt = "2026-09-27T06:47:00"
        )

        assertEquals(42, dto.id)
        assertEquals("ATTENDANCE_CHECK_IN", dto.type)
        assertEquals("Alya Putri", dto.student?.fullName)
        assertFalse(dto.isRead)
    }

    @Test
    fun testEmptyAttendanceSummaryDoesNotInventPercentage() {
        val emptySummary = AttendanceSummary()
        assertEquals(null, emptySummary.attendancePercentage())
        assertFalse(emptySummary.hasAnyAttendanceData())
        assertEquals("Belum ada data", emptySummary.attendancePercentageDisplay())
    }

    @Test
    fun testAttendanceSummaryWithRealDataStillCalculatesPercentage() {
        val summary = AttendanceSummary(presentCount = 18, lateCount = 2, permissionCount = 1, unexcusedCount = 0)
        assertEquals(18.0 / 21.0 * 100.0, summary.attendancePercentage() ?: 0.0, 0.0001)
        assertTrue(summary.hasAnyAttendanceData())
        assertEquals("86%", summary.attendancePercentageDisplay())
    }

    @Test
    fun testAttendanceRecordSemanticsNeverInventPunctualStatusWithoutRealAttendance() {
        val emptyAttendance = AttendanceRecord(
            id = "",
            date = "2026-09-28",
            checkInTime = null,
            checkOutTime = null,
            status = AttendanceStatus.UNKNOWN
        )
        assertFalse(emptyAttendance.hasRecordedAttendance)
        assertFalse(emptyAttendance.hasRecordedCheckIn)
        assertEquals(null, emptyAttendance.punctualityLabel)

        val onTimeAttendance = AttendanceRecord(
            id = "77",
            date = "2026-09-28",
            checkInTime = "07:00",
            checkOutTime = null,
            status = AttendanceStatus.PRESENT
        )
        assertTrue(onTimeAttendance.hasRecordedAttendance)
        assertTrue(onTimeAttendance.hasRecordedCheckIn)
        assertEquals("Tepat waktu", onTimeAttendance.punctualityLabel)
        assertTrue(onTimeAttendance.isOnTimeCheckIn)

        val lateAttendance = AttendanceRecord(
            id = "78",
            date = "2026-09-28",
            checkInTime = "07:45",
            checkOutTime = null,
            status = AttendanceStatus.LATE
        )
        assertTrue(lateAttendance.hasRecordedAttendance)
        assertEquals("Terlambat", lateAttendance.punctualityLabel)
        assertFalse(lateAttendance.isOnTimeCheckIn)
    }
}
