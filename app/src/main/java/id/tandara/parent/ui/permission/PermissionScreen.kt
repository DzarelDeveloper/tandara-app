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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.runtime.remember
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
import id.tandara.parent.core.common.DateUtils
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
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
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.LeaveStatus
import id.tandara.parent.domain.model.LeaveType
import id.tandara.parent.domain.model.Student
import id.tandara.parent.ui.components.ConfirmationDialog
import id.tandara.parent.ui.components.ConnectionStateBanner
import id.tandara.parent.ui.components.TandaraBrandTopAppBar
import id.tandara.parent.ui.components.TandaraButton
import id.tandara.parent.ui.components.TandaraOutlinedButton
import id.tandara.parent.ui.components.TandaraTextField
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    onViewAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

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
            colors = DatePickerDefaults.colors(containerColor = PrimarySurface)
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
            colors = DatePickerDefaults.colors(containerColor = PrimarySurface)
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

    if (uiState.showConfirmDialog) {
        ConfirmationDialog(
            title = "Kirim pengajuan izin?",
            message = "Pengajuan izin untuk ${uiState.selectedStudent?.name ?: "Siswa"} akan diperiksa dan diverifikasi oleh guru piket.",
            confirmLabel = "Kirim",
            onConfirm = { viewModel.confirmSubmit() },
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
            ConnectionStateBanner(isOffline = uiState.isOffline, lastUpdatedAt = uiState.lastUpdatedAt)
            Spacer(modifier = Modifier.height(14.dp))

            // ========================================================
            // HEADER: Ajukan Izin
            // ========================================================
            Text(
                text = "Ajukan Izin",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ajukan izin ketidakhadiran anak. Isi informasi berikut untuk dikirim ke sekolah.",
                fontSize = 13.5.sp,
                color = SecondaryText,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Siswa info card (No dropdown, strict single student per account)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SecondarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlueLight)
                            .border(1.dp, AccentBlue.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState.selectedStudent?.name?.firstOrNull() ?: "S").toString(),
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Siswa",
                            fontSize = 11.5.sp,
                            color = SecondaryText
                        )
                        Text(
                            text = uiState.selectedStudent?.name ?: "Memuat data siswa...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                        Text(
                            text = uiState.selectedStudent?.className ?: "",
                            fontSize = 12.5.sp,
                            color = SecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ========================================================
            // FORM: Jenis Izin
            // ========================================================
            Text(
                text = "Jenis Izin",
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

            Spacer(modifier = Modifier.height(18.dp))

            // Tanggal
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
                        Text(text = "Mulai", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
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
                        Text(text = "Sampai", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
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

            Spacer(modifier = Modifier.height(18.dp))

            // Alasan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alasan",
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
                label = "Tulis alasan ketidakhadiran",
                placeholder = "Jelaskan alasan izin atau kondisi anak (minimal 10 karakter)...",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                errorMessage = uiState.reasonError,
                testTag = "input_reason"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Lampiran
            Text(
                text = "Lampiran (Opsional)",
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
                    Text(text = "Tambah bukti", color = PrimaryText)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Kirim Pengajuan button with loading
            TandaraButton(
                onClick = { viewModel.requestSubmit() },
                enabled = uiState.canSubmit && !uiState.isSubmitting,
                isLoading = uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                testTag = "button_submit_permission"
            ) {
                Text(
                    text = if (uiState.isSubmitting) "Mengirim..." else "Kirim Pengajuan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!uiState.canSubmit && !uiState.isSubmitting) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Lengkapi tanggal dan alasan minimal 10 karakter untuk mengirim.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ========================================================
            // RECENT HISTORY (3 items) + Lihat semua
            // ========================================================
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Riwayat Pengajuan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    if (uiState.leaveHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Terbaru",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                }
                if (uiState.leaveHistory.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onViewAllHistory)
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("button_lihat_semua"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lihat semua",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentBlue
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.leaveHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimarySurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            uiState.selectedStudent == null -> "Data siswa belum tersedia."
                            uiState.leaveHistoryAvailable -> "Belum ada riwayat izin."
                            else -> "Riwayat izin belum dapat dimuat."
                        },
                        color = SecondaryText,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.recentHistory.forEach { request ->
                        LeaveHistoryCard(
                            request = request,
                            student = uiState.selectedStudent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable permission history card. Status comes from actual model status.
 * Shows minimal info consistent with available backend fields: type, status, startDate, reason, submittedAt.
 * No fabricated fields (teacher note, response, etc.) that are not part of LeaveRequest.
 */
@Composable
fun LeaveHistoryCard(
    request: LeaveRequest,
    student: Student?,
    modifier: Modifier = Modifier
) {
    val status = request.status.displayNameShort
    val statusColor = when (request.status) {
        LeaveStatus.APPROVED -> SuccessGreen
        LeaveStatus.REJECTED -> ErrorRed
        else -> WarningAmber
    }
    val statusBg = when (request.status) {
        LeaveStatus.APPROVED -> SuccessGreenBg
        LeaveStatus.REJECTED -> ErrorRed.copy(alpha = 0.08f)
        else -> WarningAmberBg
    }
    val submittedText = request.submittedAt.takeIf { it.isNotBlank() }?.let { formatSubmittedText(it) }
    val teacherResponse = request.reviewerNote
        ?.trim()
        ?.takeIf { it.isNotEmpty() && request.status != LeaveStatus.PENDING }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PrimarySurface)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("leave_history_card_${request.id.ifBlank { request.startDate }}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.type.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
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

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatDateRange(request.startDate, request.endDate),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )

            if (student != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${student.name} • ${student.className}",
                    fontSize = 12.sp,
                    color = SecondaryText
                )
            }

            if (request.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Alasan izin",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = request.reason,
                    fontSize = 12.5.sp,
                    color = PrimaryText.copy(alpha = 0.9f),
                    lineHeight = 17.sp,
                    maxLines = 3
                )
            }

            if (teacherResponse != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondarySurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (request.status == LeaveStatus.REJECTED) {
                            "Alasan dari Guru"
                        } else {
                            "Pesan dari Guru"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = teacherResponse,
                        fontSize = 12.5.sp,
                        color = PrimaryText,
                        lineHeight = 17.sp
                    )
                }
            }

            if (submittedText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Diajukan • $submittedText",
                    fontSize = 11.5.sp,
                    color = SecondaryText
                )
            }
        }
    }
}

private val LeaveStatus.displayNameShort: String
    get() = when (this) {
        LeaveStatus.PENDING -> "Menunggu"
        LeaveStatus.APPROVED -> "Disetujui"
        LeaveStatus.REJECTED -> "Ditolak"
    }

private fun formatDateRange(start: String, end: String): String {
    val localeId = Locale("id", "ID")
    val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val outFmt = SimpleDateFormat("dd MMMM yyyy", localeId)
    val outFmtShort = SimpleDateFormat("dd MMMM yyyy", localeId)
    return try {
        val s = apiFmt.parse(start)
        val e = apiFmt.parse(end)
        if (s == null || e == null) start
        else if (s == e) outFmt.format(s)
        else {
            val calS = Calendar.getInstance().apply { time = s }
            val calE = Calendar.getInstance().apply { time = e }
            if (calS.get(Calendar.MONTH) == calE.get(Calendar.MONTH) &&
                calS.get(Calendar.YEAR) == calE.get(Calendar.YEAR)
            ) {
                "${calS.get(Calendar.DAY_OF_MONTH)}–${calE.get(Calendar.DAY_OF_MONTH)} ${outFmt.format(e).dropWhile { !it.isLetter() }}"
            } else {
                "${outFmtShort.format(s)} – ${outFmtShort.format(e)}"
            }
        }
    } catch (_: Exception) {
        if (start == end) start else "$start – $end"
    }
}

private fun formatSubmittedText(createdAt: String): String? {
    val localeId = Locale("id", "ID")
    val candidates = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )
    val outDate = SimpleDateFormat("dd MMM", localeId)
    val outTime = SimpleDateFormat("HH:mm", localeId)
    for (fmt in candidates) {
        try {
            val d: Date = fmt.parse(createdAt) ?: continue
            return "${outDate.format(d)} · ${outTime.format(d)}"
        } catch (_: Exception) { /* try next */ }
    }
    return null
}
