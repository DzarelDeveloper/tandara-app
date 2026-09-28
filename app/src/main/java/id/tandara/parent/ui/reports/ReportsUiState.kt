package id.tandara.parent.ui.reports

import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.Student

data class ReportsUiState(
    val currentStudent: Student? = null,
    val selectedMonthYear: String = "",
    val presentCountDisplay: String = "0",
    val lateCountDisplay: String = "0",
    val permissionCountDisplay: String = "0",
    val sickCountDisplay: String = "0",
    val unexcusedCountDisplay: String = "0",
    val records: List<AttendanceRecord> = emptyList(),
    val isDownloadAvailable: Boolean = false,
    val showDownloadInfoDialog: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
) {
    val selectedStudent: Student? get() = currentStudent
    val linkedStudents: List<Student> get() = currentStudent?.let { listOf(it) } ?: emptyList()
}
