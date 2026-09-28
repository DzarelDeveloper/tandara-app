package id.tandara.parent.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.tandara.parent.core.common.DateUtils
import id.tandara.parent.core.network.ApiResult
import id.tandara.parent.domain.model.LeaveAttachment
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.LeaveType
import id.tandara.parent.domain.model.Student
import id.tandara.parent.domain.repository.ParentRepository
import id.tandara.parent.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PermissionViewModel(
    private val parentRepository: ParentRepository,
    private val permissionRepository: PermissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        loadStudent()
    }

    private fun loadStudent() {
        viewModelScope.launch {
            val result = parentRepository.getLinkedStudents()
            if (result is ApiResult.Success) {
                val student = result.data.firstOrNull()
                _uiState.update { it.copy(selectedStudent = student, isLoading = true) }
                validateForm()
                loadLeaveHistory(student?.id)
            }
        }
    }

    private fun loadLeaveHistory(studentId: String? = _uiState.value.selectedStudent?.id) {
        if (studentId.isNullOrBlank()) return
        viewModelScope.launch {
            val result = permissionRepository.getLeaveHistory(studentId)
            if (result is ApiResult.Success) {
                _uiState.update { it.copy(leaveHistory = result.data, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTypeSelected(type: LeaveType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun openStartDatePicker() {
        _uiState.update { it.copy(showStartDatePicker = true) }
    }

    fun closeStartDatePicker() {
        _uiState.update { it.copy(showStartDatePicker = false) }
    }

    fun onStartDateSelected(millis: Long) {
        val dateText = DateUtils.formatToShortDate(millis)
        _uiState.update {
            it.copy(
                startDateMillis = millis,
                startDateText = dateText,
                showStartDatePicker = false
            )
        }
        // If end date is earlier than start date, reset end date
        val end = _uiState.value.endDateMillis
        if (end != null && end < millis) {
            _uiState.update {
                it.copy(
                    endDateMillis = millis,
                    endDateText = dateText,
                    dateError = null
                )
            }
        }
        validateForm()
    }

    fun openEndDatePicker() {
        _uiState.update { it.copy(showEndDatePicker = true) }
    }

    fun closeEndDatePicker() {
        _uiState.update { it.copy(showEndDatePicker = false) }
    }

    fun onEndDateSelected(millis: Long) {
        val start = _uiState.value.startDateMillis
        if (start != null && millis < start) {
            _uiState.update {
                it.copy(
                    dateError = "Tanggal akhir tidak boleh lebih awal dari tanggal mulai.",
                    showEndDatePicker = false
                )
            }
        } else {
            val dateText = DateUtils.formatToShortDate(millis)
            _uiState.update {
                it.copy(
                    endDateMillis = millis,
                    endDateText = dateText,
                    dateError = null,
                    showEndDatePicker = false
                )
            }
        }
        validateForm()
    }

    fun onReasonChanged(value: String) {
        if (value.length <= 500) {
            _uiState.update { it.copy(reason = value) }
            validateForm()
        }
    }

    fun onAttachmentSelected(fileName: String, fileType: String, sizeBytes: Long, uriString: String) {
        val attachment = LeaveAttachment(
            fileName = fileName,
            fileType = fileType,
            fileSizeBytes = sizeBytes,
            localUriString = uriString
        )
        _uiState.update { it.copy(attachment = attachment) }
    }

    fun removeAttachment() {
        _uiState.update { it.copy(attachment = null) }
    }

    fun togglePreviewValidation() {
        _uiState.update {
            val nextState = !it.previewValidationActive
            it.copy(previewValidationActive = nextState)
        }
        validateForm()
    }

    private fun validateForm() {
        val current = _uiState.value
        val reasonLen = current.reason.trim().length
        val reasonError = when {
            reasonLen == 0 -> null
            reasonLen < 10 -> "Alasan izin minimal 10 karakter (saat ini $reasonLen)."
            reasonLen > 500 -> "Alasan izin maksimal 500 karakter."
            else -> null
        }

        val hasDates = current.startDateMillis != null && current.endDateMillis != null
        val datesValid = hasDates && (current.dateError == null) &&
                (current.endDateMillis!! >= current.startDateMillis!!)

        val isReasonValid = reasonLen in 10..500
        val isFormValid = hasDates && datesValid && isReasonValid

        // Submit requires linked student (or preview validation mode for verification)
        val canSubmit = isFormValid && (current.selectedStudent != null || current.previewValidationActive)

        _uiState.update {
            it.copy(
                reasonError = reasonError,
                isFormValid = isFormValid,
                canSubmit = canSubmit
            )
        }
    }

    fun requestSubmit() {
        if (_uiState.value.canSubmit) {
            _uiState.update { it.copy(showConfirmDialog = true) }
        }
    }

    fun dismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun confirmSubmit() {
        _uiState.update { it.copy(showConfirmDialog = false) }
        viewModelScope.launch {
            val current = _uiState.value
            val request = LeaveRequest(
                studentId = current.selectedStudent?.id ?: "unknown",
                type = current.selectedType,
                startDate = current.startDateText,
                endDate = current.endDateText,
                reason = current.reason,
                attachment = current.attachment
            )
            val result = permissionRepository.submitLeaveRequest(request)
            when (result) {
                is ApiResult.BackendUnavailable -> {
                    _uiState.update {
                        it.copy(snackbarMessage = result.message)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(snackbarMessage = result.message)
                    }
                }
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = "Pengajuan izin berhasil dikirim.",
                            leaveHistory = listOf(result.data) + it.leaveHistory,
                            reason = "",
                            startDateText = "",
                            endDateText = "",
                            startDateMillis = null,
                            endDateMillis = null,
                            selectedType = LeaveType.PERMISSION
                        )
                    }
                    loadLeaveHistory(current.selectedStudent?.id)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(
        private val parentRepository: ParentRepository,
        private val permissionRepository: PermissionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PermissionViewModel(parentRepository, permissionRepository) as T
        }
    }
}
