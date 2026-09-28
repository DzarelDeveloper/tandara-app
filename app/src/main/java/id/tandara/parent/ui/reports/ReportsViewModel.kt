package id.tandara.parent.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.data.realtime.ParentRealtimeCoordinator
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.domain.model.Student
import id.tandara.parent.domain.repository.AttendanceRepository
import id.tandara.parent.domain.repository.ParentRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Reports Screen (UI/UX V3).
 * Strictly bound to the authenticated Parent's assigned Student.
 */
class ReportsViewModel(
    private val parentRepository: ParentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val realtimeCoordinator: ParentRealtimeCoordinator
) : ViewModel() {

    private var selectedMonth = LocalDate.now().withDayOfMonth(1)

    private val _uiState = MutableStateFlow(
        ReportsUiState(selectedMonthYear = LocalDate.now().withDayOfMonth(1).let { month ->
            "${java.time.Month.of(month.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${month.year}"
        })
    )
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            realtimeCoordinator.networkConnected.collect { connected ->
                if (!connected) _uiState.update { it.copy(isOffline = true) }
            }
        }
        viewModelScope.launch {
            realtimeCoordinator.refreshEvents.collect { loadData() }
        }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.records.isEmpty()) }
            val studentResult = parentRepository.getAssignedStudent()
            if (studentResult is ApiResult.Success) {
                val student = studentResult.data
                val currentMonth = selectedMonth
                val recordsResult = attendanceRepository.getMonthlyAttendanceRecords(student.id, currentMonth.monthValue, currentMonth.year)

                val records = (recordsResult as? ApiResult.Success)?.data ?: emptyList()
                val recordsAvailable = recordsResult is ApiResult.Success
                val stale = studentResult.isStale || (recordsResult as? ApiResult.Success)?.isStale == true
                val errorMessage = when {
                    recordsResult is ApiResult.Error -> recordsResult.message
                    recordsResult is ApiResult.BackendUnavailable -> recordsResult.message
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        currentStudent = student,
                        selectedMonthYear = "${java.time.Month.of(currentMonth.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${currentMonth.year}",
                        presentCountDisplay = if (recordsAvailable) records.count { it.status == AttendanceStatus.PRESENT }.toString() else "—",
                        lateCountDisplay = if (recordsAvailable) records.count { it.status == AttendanceStatus.LATE }.toString() else "—",
                        permissionCountDisplay = if (recordsAvailable) records.count { it.status == AttendanceStatus.PERMISSION }.toString() else "—",
                        sickCountDisplay = if (recordsAvailable) records.count { it.status == AttendanceStatus.SICK }.toString() else "—",
                        unexcusedCountDisplay = if (recordsAvailable) records.count { it.status == AttendanceStatus.UNEXCUSED }.toString() else "—",
                        records = records,
                        isDownloadAvailable = records.isNotEmpty(),
                        errorMessage = errorMessage,
                        isOffline = stale || recordsResult is ApiResult.BackendUnavailable,
                        lastUpdatedAt = (recordsResult as? ApiResult.Success)?.lastUpdatedAt,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Data siswa tidak tersedia.") }
            }
        }
    }

    fun goToPreviousMonth() {
        selectedMonth = selectedMonth.minusMonths(1)
        val previousMonth = selectedMonth
        _uiState.update {
            it.copy(
                selectedMonthYear = "${java.time.Month.of(previousMonth.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${previousMonth.year}"
            )
        }
        loadData()
    }

    fun goToNextMonth() {
        selectedMonth = selectedMonth.plusMonths(1)
        val nextMonth = selectedMonth
        _uiState.update {
            it.copy(
                selectedMonthYear = "${java.time.Month.of(nextMonth.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${nextMonth.year}"
            )
        }
        loadData()
    }

    fun triggerDownload() {
        _uiState.update { it.copy(showDownloadInfoDialog = true) }
    }

    fun dismissDownloadInfo() {
        _uiState.update { it.copy(showDownloadInfoDialog = false) }
    }

    class Factory(
        private val parentRepository: ParentRepository,
        private val attendanceRepository: AttendanceRepository,
        private val realtimeCoordinator: ParentRealtimeCoordinator
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(parentRepository, attendanceRepository, realtimeCoordinator) as T
        }
    }
}
