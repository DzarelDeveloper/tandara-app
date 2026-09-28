package id.tandara.parent.ui.permission

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.ErrorRed
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimaryBlueLight
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.core.designsystem.SuccessGreenBg
import id.tandara.parent.core.designsystem.WarningAmber
import id.tandara.parent.core.designsystem.WarningAmberBg
import id.tandara.parent.domain.model.LeaveType
import id.tandara.parent.ui.components.ConfirmationDialog
import id.tandara.parent.ui.components.TandaraBrandTopAppBar
import id.tandara.parent.ui.components.TandaraButton
import id.tandara.parent.ui.components.TandaraOutlinedButton
import id.tandara.parent.ui.components.TandaraTextField

/**
 * Leave / Izin Screen (UI/UX V3).
 * Redesigned with Dark Navy Layered Interface.
 * STRICT ONE PARENT ACCOUNT = ONE STUDENT (No student selector anywhere).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Riwayat Izin, 1: Formulir Izin

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    // Attachment file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "lampiran"
            var fileSize = 0L
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            viewModel.onAttachmentSelected(
                fileName = fileName,
                fileType = mimeType,
                sizeBytes = fileSize,
                uriString = uri.toString()
            )
        }
    }

    // Start Date Picker Dialog
    if (uiState.showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.closeStartDatePicker() },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onStartDateSelected(it)
                        }
                    },
                    modifier = Modifier.testTag("date_picker_start_confirm")
                ) {
                    Text("Pilih", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeStartDatePicker() }) {
                    Text("Batal", color = SecondaryText)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = PrimarySurface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = PrimarySurface,
                    titleContentColor = PrimaryText,
                    headlineContentColor = PrimaryText,
                    weekdayContentColor = SecondaryText,
                    subheadContentColor = SecondaryText,
                    yearContentColor = PrimaryText,
                    currentYearContentColor = AccentBlue,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = PrimaryBlue,
                    dayContentColor = PrimaryText,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = PrimaryBlue,
                    todayContentColor = AccentBlue,
                    todayDateBorderColor = AccentBlue
                )
            )
        }
    }

    // End Date Picker Dialog
    if (uiState.showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.endDateMillis ?: (uiState.startDateMillis ?: System.currentTimeMillis())
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.closeEndDatePicker() },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onEndDateSelected(it)
                        }
                    },
                    modifier = Modifier.testTag("date_picker_end_confirm")
                ) {
                    Text("Pilih", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeEndDatePicker() }) {
                    Text("Batal", color = SecondaryText)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = PrimarySurface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = PrimarySurface,
                    titleContentColor = PrimaryText,
                    headlineContentColor = PrimaryText,
                    weekdayContentColor = SecondaryText,
                    subheadContentColor = SecondaryText,
                    yearContentColor = PrimaryText,
                    currentYearContentColor = AccentBlue,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = PrimaryBlue,
                    dayContentColor = PrimaryText,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = PrimaryBlue,
                    todayContentColor = AccentBlue,
                    todayDateBorderColor = AccentBlue
                )
            )
        }
    }

    // Submission Confirmation Dialog
    if (uiState.showConfirmDialog) {
        ConfirmationDialog(
            title = "Kirim pengajuan izin?",
            message = "Pengajuan izin untuk ${uiState.selectedStudent?.name ?: "Siswa"} akan diperiksa dan diverifikasi oleh guru piket.",
            confirmLabel = "Kirim",
            onConfirm = {
                viewModel.confirmSubmit()
                selectedTab = 0
            },
            onDismiss = { viewModel.dismissConfirmDialog() },
            testTag = "submit_leave_confirm_dialog"
        )
    }

    Scaffold(
        topBar = {
            TandaraBrandTopAppBar(
                onNotificationClick = {},
                tagline = "Izin & Ketidakhadiran Siswa",
                hasUnreadNotifications = false
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 36.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header: Izin & Ketidakhadiran + Student Context (Fixed single student, NO dropdown)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Izin & Ketidakhadiran",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${uiState.selectedStudent?.name ?: "Siswa"} • ${uiState.selectedStudent?.className ?: "Kelas belum ditentukan"}",
                        fontSize = 13.5.sp,
                        color = SecondaryText
                    )
                }

                // Primary Button: + Ajukan Izin
                if (selectedTab == 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryBlue)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                            .testTag("button_open_leave_form"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ajukan Izin",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Switcher: Riwayat Pengajuan vs Formulir Pengajuan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimarySurface, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selectedTab == 0) SecondarySurface else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_history_izin"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Riwayat Pengajuan",
                        fontSize = 13.5.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) AccentBlue else SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selectedTab == 1) SecondarySurface else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_form_izin"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Formulir Baru",
                        fontSize = 13.5.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) AccentBlue else SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 1) {
                // ========================================================
                // LEAVE FORM (No Student Selector!)
                // ========================================================

                // Info banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlueLight)
                        .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pengajuan izin akan langsung diteruskan kepada guru piket & wali kelas.",
                            fontSize = 13.sp,
                            color = PrimaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Jenis Ketidakhadiran: [ Izin ] [ Sakit ] [ Lainnya ]
                Text(
                    text = "Jenis Ketidakhadiran",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeaveType.entries.forEach { type ->
                        val isSelected = uiState.selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryBlueLight else PrimarySurface)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentBlue else BorderColor,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.onTypeSelected(type) }
                                .padding(vertical = 12.dp)
                                .testTag("leave_type_${type.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) AccentBlue else PrimaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dates: Start Date & End Date Pickers
                Text(
                    text = "Rentang Tanggal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimarySurface)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .clickable { viewModel.openStartDatePicker() }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("picker_start_date")
                    ) {
                        Column {
                            Text(
                                text = "Mulai",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.startDateText.ifEmpty { "Pilih tanggal" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.startDateText.isNotEmpty()) PrimaryText else SecondaryText
                                )
                            }
                        }
                    }

                    // End Date
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimarySurface)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .clickable { viewModel.openEndDatePicker() }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("picker_end_date")
                    ) {
                        Column {
                            Text(
                                text = "Sampai",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.endDateText.ifEmpty { "Pilih tanggal" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.endDateText.isNotEmpty()) PrimaryText else SecondaryText
                                )
                            }
                        }
                    }
                }

                if (uiState.dateError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.dateError!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reason Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alasan Ketidakhadiran",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Text(
                        text = "${uiState.reason.length}/500",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.reason.length > 500) ErrorRed else SecondaryText,
                        modifier = Modifier.testTag("reason_char_counter")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                TandaraTextField(
                    value = uiState.reason,
                    onValueChange = viewModel::onReasonChanged,
                    label = "Tuliskan keterangan detail",
                    placeholder = "Jelaskan alasan izin atau kondisi anak (minimal 10 karakter)...",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    errorMessage = uiState.reasonError,
                    testTag = "input_reason"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Attachment Section (File Picker)
                Text(
                    text = "Lampiran Surat / Bukti (Opsional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Format: JPG, PNG, PDF (Maks. 5 MB)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.attachment != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimarySurface)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                            .testTag("attachment_info_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = uiState.attachment!!.fileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = PrimaryText,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = uiState.attachment!!.fileType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryText
                                    )
                                }
                            }
                            IconButton(
                                onClick = viewModel::removeAttachment,
                                modifier = Modifier.testTag("button_remove_attachment")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Hapus lampiran",
                                    tint = ErrorRed
                                )
                            }
                        }
                    }
                } else {
                    TandaraOutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "button_select_attachment"
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Pilih Dokumen / Foto", color = PrimaryText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Submit Button
                TandaraButton(
                    onClick = { viewModel.requestSubmit() },
                    enabled = uiState.canSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "button_submit_permission"
                ) {
                    Text(
                        text = "Kirim Pengajuan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!uiState.canSubmit) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Lengkapi tanggal dan alasan minimal 10 karakter untuk mengirim.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                PermissionHistoryList(
                    history = uiState.leaveHistory,
                    selectedStudent = uiState.selectedStudent
                )
            }
        }
    }
}

@Composable
private fun PermissionHistoryList(
    history: List<id.tandara.parent.domain.model.LeaveRequest>,
    selectedStudent: id.tandara.parent.domain.model.Student?
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimarySurface, RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada riwayat izin.",
                color = SecondaryText,
                fontSize = 14.sp
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        history.forEach { request ->
            val status = request.status.displayName
            val statusColor = when (request.status) {
                id.tandara.parent.domain.model.LeaveStatus.APPROVED -> SuccessGreen
                id.tandara.parent.domain.model.LeaveStatus.REJECTED -> ErrorRed
                else -> WarningAmber
            }
            val statusBg = when (request.status) {
                id.tandara.parent.domain.model.LeaveStatus.APPROVED -> SuccessGreenBg
                id.tandara.parent.domain.model.LeaveStatus.REJECTED -> ErrorRed.copy(alpha = 0.08f)
                else -> WarningAmberBg
            }

            PermissionHistoryCard(
                studentName = selectedStudent?.name ?: "Siswa",
                dateRange = "${request.startDate} • ${request.endDate}",
                leaveType = request.type.displayName,
                status = status,
                statusColor = statusColor,
                statusBg = statusBg,
                reason = request.reason,
                reviewer = when (request.status) {
                    id.tandara.parent.domain.model.LeaveStatus.APPROVED -> "Disetujui oleh guru piket"
                    id.tandara.parent.domain.model.LeaveStatus.REJECTED -> "Ditolak oleh guru piket"
                    else -> "Menunggu verifikasi guru piket"
                }
            )
        }
    }
}

@Composable
private fun PermissionHistoryCard(
    studentName: String,
    dateRange: String,
    leaveType: String,
    status: String,
    statusColor: Color,
    statusBg: Color,
    reason: String,
    reviewer: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PrimarySurface)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = studentName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$leaveType • $dateRange",
                        fontSize = 12.5.sp,
                        color = SecondaryText
                    )
                }

                // Clear Status Chip: Menunggu / Disetujui / Ditolak
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "\"$reason\"",
                fontSize = 13.sp,
                color = PrimaryText.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reviewer,
                fontSize = 11.5.sp,
                color = SecondaryText
            )
        }
    }
}
