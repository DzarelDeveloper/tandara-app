package id.tandara.parent.ui.home

import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.AttendanceSummary
import id.tandara.parent.domain.model.Parent
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.model.Student

/**
 * UI State for Home Screen (UI/UX V3).
 * Strictly binds to exactly ONE assigned student context.
 */
data class HomeUiState(
    val currentParent: Parent? = null,
    val greeting: String = "Selamat pagi",
    val formattedDate: String = "",
    val monthYearText: String = "",
    val currentStudent: Student? = null,
    val todayAttendance: AttendanceRecord? = null,
    val todayAttendanceAvailable: Boolean = false,
    val monthlySummary: AttendanceSummary? = null,
    val notifications: List<ParentNotification> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isLoading: Boolean = false,
    val showNotificationSheet: Boolean = false,
    val showStudentDetail: Boolean = false,
    val isOffline: Boolean = false,
    val lastUpdatedAt: Long? = null,
    val realtimeBannerMessage: String? = null,
    val showRealtimeBanner: Boolean = false,
    val snackbarMessage: String? = null
) {
    // Backwards-compatibility property
    val selectedStudent: Student? get() = currentStudent
    val linkedStudents: List<Student> get() = currentStudent?.let { listOf(it) } ?: emptyList()
}
