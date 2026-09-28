package id.tandara.parent.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.realtime.ParentRealtimeCoordinator
import id.tandara.parent.domain.model.ParentNotification
import id.tandara.parent.domain.repository.AttendanceRepository
import id.tandara.parent.domain.repository.AuthRepository
import id.tandara.parent.domain.repository.ParentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel for Tandara Home Screen (UI/UX V3).
 * Strict One Parent Account = One Student.
 * Dynamic Greeting & Date from local device time.
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val parentRepository: ParentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val realtimeCoordinator: ParentRealtimeCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            greeting = computeDynamicGreeting(),
            formattedDate = computeFormattedDate(),
            monthYearText = computeMonthYearText()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeParentSession()
        observeRealtimeEvents()
        observeRecoveryEvents()
        loadHomeData()
    }

    private fun observeRecoveryEvents() {
        viewModelScope.launch {
            realtimeCoordinator.networkConnected.collect { connected ->
                if (!connected) _uiState.update { it.copy(isOffline = true) }
            }
        }
        viewModelScope.launch {
            realtimeCoordinator.refreshEvents.collect { loadHomeData() }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.currentStudent == null,
                    greeting = computeDynamicGreeting(),
                    formattedDate = computeFormattedDate(),
                    monthYearText = computeMonthYearText()
                )
            }
            val studentResult = parentRepository.getAssignedStudent()
            if (studentResult is ApiResult.Success) {
                val student = studentResult.data
                val todayAttRes = attendanceRepository.getTodayAttendance(student.id)
                val todayAtt = (todayAttRes as? ApiResult.Success)?.data

                val now = LocalDate.now()
                val summaryRes = attendanceRepository.getMonthlyAttendanceSummary(student.id, now.monthValue, now.year)
                val summary = (summaryRes as? ApiResult.Success)?.data

                val notifRes = parentRepository.getNotifications()
                val notifs = (notifRes as? ApiResult.Success)?.data ?: emptyList()
                val unreadCountRes = parentRepository.getUnreadNotificationCount()
                val unreadCount = (unreadCountRes as? ApiResult.Success)?.data ?: 0
                val successful = listOf(todayAttRes, summaryRes, notifRes, unreadCountRes).filterIsInstance<ApiResult.Success<*>>()
                val isOffline = studentResult.isStale || successful.any { it.isStale } ||
                    listOf(todayAttRes, summaryRes, notifRes, unreadCountRes).any { it is ApiResult.BackendUnavailable }
                val lastUpdated = successful.mapNotNull { it.lastUpdatedAt }.minOrNull()

                _uiState.update {
                    it.copy(
                        currentStudent = student,
                        todayAttendance = todayAtt,
                        todayAttendanceAvailable = todayAttRes is ApiResult.Success,
                        monthlySummary = summary,
                        notifications = dedupeNotifications(notifs),
                        unreadNotificationCount = unreadCount,
                        isOffline = isOffline,
                        lastUpdatedAt = lastUpdated,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, isOffline = studentResult is ApiResult.BackendUnavailable,
                    snackbarMessage = "Tidak dapat memuat data. Hubungkan perangkat ke jaringan Tandara dan coba lagi.") }
            }
        }
    }

    fun openStudentDetail() {
        _uiState.update { it.copy(showStudentDetail = true) }
    }

    fun closeStudentDetail() {
        _uiState.update { it.copy(showStudentDetail = false) }
    }

    fun openNotificationSheet() {
        _uiState.update { it.copy(showNotificationSheet = true) }
    }

    fun closeNotificationSheet() {
        _uiState.update { it.copy(showNotificationSheet = false) }
    }

    fun dismissRealtimeBanner() {
        _uiState.update { it.copy(showRealtimeBanner = false) }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun observeParentSession() {
        viewModelScope.launch {
            authRepository.currentParentSession.collect { parent ->
                _uiState.update { it.copy(currentParent = parent) }
            }
        }
    }

    private fun observeRealtimeEvents() {
        viewModelScope.launch {
            realtimeCoordinator.events.collect { event ->
                val nextNotifications = dedupeNotifications(
                    listOf(event.notification) + _uiState.value.notifications
                )
                val unreadCount = nextNotifications.count { !it.isRead }
                _uiState.update { it.copy(notifications = nextNotifications, unreadNotificationCount = unreadCount) }

                val studentId = _uiState.value.currentStudent?.id ?: return@collect
                val studentMatches = event.notification.relatedStudentId == null || event.notification.relatedStudentId == studentId
                if (!studentMatches) return@collect

                when (event.type) {
                    "STUDENT_CHECK_IN",
                    "STUDENT_CHECK_OUT",
                    "ATTENDANCE_CORRECTED" -> {
                        loadHomeData()
                    }
                    "LEAVE_APPROVED",
                    "LEAVE_REJECTED" -> {
                        loadHomeData()
                    }
                }
            }
        }
    }

    private fun dedupeNotifications(items: List<ParentNotification>): List<ParentNotification> {
        val byId = linkedMapOf<String, ParentNotification>()
        items.forEach { item ->
            val key = item.id.ifBlank { item.title + item.message + item.timestamp }
            if (!byId.containsKey(key)) byId[key] = item
        }
        return byId.values.toList()
    }

    companion object {
        /**
         * Dynamic greeting based on actual local device time:
         * 05:00–10:59 -> Selamat pagi
         * 11:00–14:59 -> Selamat siang
         * 15:00–17:59 -> Selamat sore
         * 18:00–04:59 -> Selamat malam
         */
        fun computeDynamicGreeting(localTime: LocalTime = LocalTime.now()): String {
            val hour = localTime.hour
            return when (hour) {
                in 5..10 -> "Selamat pagi"
                in 11..14 -> "Selamat siang"
                in 15..17 -> "Selamat sore"
                else -> "Selamat malam"
            }
        }

        fun computeFormattedDate(date: LocalDate = LocalDate.now()): String {
            return try {
                val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))
                date.format(formatter)
            } catch (e: Exception) {
                val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
                date.format(formatter)
            }
        }

        fun computeMonthYearText(date: LocalDate = LocalDate.now()): String {
            return try {
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))
                date.format(formatter)
            } catch (e: Exception) {
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                date.format(formatter)
            }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val parentRepository: ParentRepository,
        private val attendanceRepository: AttendanceRepository,
        private val realtimeCoordinator: ParentRealtimeCoordinator
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(authRepository, parentRepository, attendanceRepository, realtimeCoordinator) as T
        }
    }
}
