package id.tandara.parent.ui.permission

import id.tandara.parent.domain.model.LeaveAttachment
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.LeaveType
import id.tandara.parent.domain.model.Student

data class PermissionUiState(
    val selectedStudent: Student? = null,
    val leaveHistory: List<LeaveRequest> = emptyList(),
    val leaveHistoryAvailable: Boolean = false,
    val selectedType: LeaveType = LeaveType.SICK,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val startDateText: String = "",
    val endDateText: String = "",
    val reason: String = "",
    val reasonError: String? = null,
    val dateError: String? = null,
    val attachment: LeaveAttachment? = null,
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val isFormValid: Boolean = false,
    val canSubmit: Boolean = false, // true only if student linked & form valid
    val snackbarMessage: String? = null,
    val previewValidationActive: Boolean = false,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val lastUpdatedAt: Long? = null
)
