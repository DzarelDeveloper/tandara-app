package id.tandara.parent.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.tandara.parent.core.network.ApiResult
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
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReportsUiState(selectedMonthYear = LocalDate.now().withDayOfMonth(1).let { month ->
            "${java.time.Month.of(month.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${month.year}"
        })
    )
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val studentResult = parentRepository.getAssignedStudent()
            if (studentResult is ApiResult.Success) {
                val student = studentResult.data
                val currentMonth = LocalDate.now().withDayOfMonth(1)
                val summaryResult = attendanceRepository.getMonthlyAttendanceSummary(student.id, currentMonth.monthValue, currentMonth.year)
                val recordsResult = attendanceRepository.getMonthlyAttendanceRecords(student.id, currentMonth.monthValue, currentMonth.year)

                val summary = (summaryResult as? ApiResult.Success)?.data
                val records = (recordsResult as? ApiResult.Success)?.data ?: emptyList()
                val errorMessage = when {
                    summaryResult is ApiResult.Error -> summaryResult.message
                    summaryResult is ApiResult.BackendUnavailable -> summaryResult.message
                    recordsResult is ApiResult.Error -> recordsResult.message
                    recordsResult is ApiResult.BackendUnavailable -> recordsResult.message
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        currentStudent = student,
                        selectedMonthYear = "${java.time.Month.of(currentMonth.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${currentMonth.year}",
                        presentCountDisplay = summary?.presentCount?.toString() ?: "0",
                        lateCountDisplay = summary?.lateCount?.toString() ?: "0",
                        permissionCountDisplay = summary?.permissionCount?.toString() ?: "0",
                        sickCountDisplay = "0",
                        unexcusedCountDisplay = summary?.unexcusedCount?.toString() ?: "0",
                        records = records,
                        isDownloadAvailable = records.isNotEmpty(),
                        errorMessage = errorMessage,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Data siswa tidak tersedia.") }
            }
        }
    }

    fun goToPreviousMonth() {
        val currentMonth = LocalDate.now().withDayOfMonth(1)
        val previousMonth = currentMonth.minusMonths(1)
        _uiState.update {
            it.copy(
                selectedMonthYear = "${java.time.Month.of(previousMonth.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${previousMonth.year}"
            )
        }
    }

    fun goToNextMonth() {
        val currentMonth = LocalDate.now().withDayOfMonth(1)
        val nextMonth = currentMonth.plusMonths(1)
        _uiState.update {
            it.copy(
                selectedMonthYear = "${java.time.Month.of(nextMonth.monthValue).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("id", "ID"))} ${nextMonth.year}"
            )
        }
    }

    fun triggerDownload() {
        _uiState.update { it.copy(showDownloadInfoDialog = true) }
    }

    fun dismissDownloadInfo() {
        _uiState.update { it.copy(showDownloadInfoDialog = false) }
    }

    class Factory(
        private val parentRepository: ParentRepository,
        private val attendanceRepository: AttendanceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(parentRepository, attendanceRepository) as T
        }
    }
}
