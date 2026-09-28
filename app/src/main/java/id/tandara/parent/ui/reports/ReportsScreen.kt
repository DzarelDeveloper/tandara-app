package id.tandara.parent.ui.reports

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.ErrorRed
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.core.designsystem.WarningAmber
import id.tandara.parent.domain.model.AttendanceRecord
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.ui.components.TandaraBrandTopAppBar
import id.tandara.parent.ui.components.ConnectionStateBanner

/**
 * Reports Screen (UI/UX V3).
 * Redesigned with Dark Navy Layered Interface.
 * STRICT ONE PARENT ACCOUNT = ONE STUDENT (No student switcher).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("Semua") }
    var selectedRecordForDetail by remember { mutableStateOf<AttendanceRecord?>(null) }

    val detailSheetState = rememberModalBottomSheetState()

    // Download / Info Dialog
    if (uiState.showDownloadInfoDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDownloadInfo() },
            title = {
                Text(
                    text = "Unduh Laporan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            },
            text = {
                Text(
                    text = "Rekap kehadiran periode ${uiState.selectedMonthYear} untuk ${uiState.currentStudent?.name ?: "Siswa"} siap diunduh dalam format PDF resmi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dismissDownloadInfo() },
                    modifier = Modifier.testTag("dialog_dismiss_download_info")
                ) {
                    Text(text = "Mengerti", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = PrimarySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Record Detail Bottom Sheet
    if (selectedRecordForDetail != null) {
        val record = selectedRecordForDetail!!
        ModalBottomSheet(
            onDismissRequest = { selectedRecordForDetail = null },
            sheetState = detailSheetState,
            containerColor = PrimarySurface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.testTag("record_detail_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rincian Presensi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    IconButton(
                        onClick = { selectedRecordForDetail = null },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Tutup", tint = PrimaryText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySurface, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportDetailRow("Nama Siswa", uiState.currentStudent?.name ?: "Siswa")
                    HorizontalDivider(color = DividerColor)
                    ReportDetailRow("Tanggal", "${record.dayName}, ${record.date}")
                    HorizontalDivider(color = DividerColor)
                    ReportDetailRow("Status Kehadiran", getStatusLabel(record.status))
                    HorizontalDivider(color = DividerColor)
                    ReportDetailRow("Presensi Masuk", record.checkInTime?.let { "$it WIB" } ?: "Belum tercatat")
                    HorizontalDivider(color = DividerColor)
                    ReportDetailRow("Presensi Pulang", record.checkOutTime?.let { "$it WIB" } ?: "Belum tercatat")
                    HorizontalDivider(color = DividerColor)
                    ReportDetailRow("Keterangan", record.note ?: "Presensi dicatat melalui pemindai wajah sekolah")
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TandaraBrandTopAppBar(
                onNotificationClick = { /* Handled via parent */ },
                tagline = "Laporan Kehadiran Siswa",
                hasUnreadNotifications = false
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                .testTag("reports_screen_content")
        ) {
            ConnectionStateBanner(isOffline = uiState.isOffline, lastUpdatedAt = uiState.lastUpdatedAt)
            Spacer(modifier = Modifier.height(14.dp))

            // Screen Header + Student Context (Fixed single student, NO dropdown)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Laporan Kehadiran",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${uiState.currentStudent?.name ?: "Siswa"} • ${uiState.currentStudent?.className ?: "Kelas belum ditentukan"}",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }

                // Download Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SecondarySurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                        .clickable(onClick = { viewModel.triggerDownload() })
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("button_download_report"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unduh",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Period Selector: < September 2026 >
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goToPreviousMonth() },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("button_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = "Bulan Sebelumnya",
                            tint = PrimaryText
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.selectedMonthYear,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }

                    IconButton(
                        onClick = { viewModel.goToNextMonth() },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("button_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = "Bulan Berikutnya",
                            tint = PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Surface: 18 Hadir | 2 Terlambat | 1 Izin | 0 Alfa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ElevatedSurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.presentCountDisplay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Hadir", fontSize = 12.sp, color = SecondaryText)
                    }
                    VerticalDivider(color = DividerColor, modifier = Modifier.height(36.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.lateCountDisplay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Terlambat", fontSize = 12.sp, color = SecondaryText)
                    }
                    VerticalDivider(color = DividerColor, modifier = Modifier.height(36.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.permissionCountDisplay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Izin", fontSize = 12.sp, color = SecondaryText)
                    }
                    VerticalDivider(color = DividerColor, modifier = Modifier.height(36.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.unexcusedCountDisplay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SecondaryText)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Alfa", fontSize = 12.sp, color = SecondaryText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Filter Chips
            Text(
                text = "Riwayat Presensi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(10.dp))

            val filters = listOf("Semua", "Hadir", "Terlambat", "Izin")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(text = filter, fontSize = 12.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondarySurface,
                            selectedLabelColor = AccentBlue,
                            containerColor = PrimarySurface,
                            labelColor = SecondaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AccentBlue else BorderColor
                        ),
                        modifier = Modifier.testTag("filter_${filter.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!uiState.errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorRed.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        fontSize = 12.5.sp,
                        color = ErrorRed
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Attendance History List (Timeline/List Style)
            val filteredRecords = uiState.records.filter { record ->
                when (selectedFilter) {
                    "Hadir" -> record.status == AttendanceStatus.PRESENT
                    "Terlambat" -> record.status == AttendanceStatus.LATE
                    "Izin" -> record.status == AttendanceStatus.PERMISSION || record.status == AttendanceStatus.SICK
                    else -> true
                }
            }

            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.errorMessage.isNullOrBlank()) {
                            "Belum ada riwayat presensi pada periode ini."
                        } else {
                            "Data riwayat belum tersedia saat ini."
                        },
                        fontSize = 13.5.sp,
                        color = SecondaryText
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredRecords.forEach { record ->
                        val (statusText, statusColor, statusBg) = getStatusConfig(record.status)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimarySurface)
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable { selectedRecordForDetail = record }
                                .padding(16.dp)
                                .testTag("record_item_${record.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Date & Day
                                Column {
                                    Text(
                                        text = record.date.substringBefore(",").ifEmpty { record.date },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = record.dayName.ifEmpty { "Hari Sekolah" },
                                        fontSize = 12.sp,
                                        color = SecondaryText
                                    )
                                }

                                // Status Pill
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
                                        text = statusText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor
                                    )
                                }

                                // Check-in & Check-out
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Masuk ", fontSize = 11.5.sp, color = SecondaryText)
                                        Text(
                                            text = record.checkInTime ?: "--:--",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Pulang ", fontSize = 11.5.sp, color = SecondaryText)
                                        Text(
                                            text = record.checkOutTime ?: "--:--",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = SecondaryText)
        Text(text = value, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
    }
}

private fun getStatusLabel(status: AttendanceStatus): String {
    return when (status) {
        AttendanceStatus.PRESENT -> "Hadir"
        AttendanceStatus.LATE -> "Terlambat"
        AttendanceStatus.PERMISSION -> "Izin"
        AttendanceStatus.SICK -> "Sakit"
        AttendanceStatus.UNEXCUSED -> "Alfa"
        AttendanceStatus.UNKNOWN -> "Belum Tercatat"
    }
}

@Composable
private fun getStatusConfig(status: AttendanceStatus): Triple<String, Color, Color> {
    val colors = id.tandara.parent.core.designsystem.TandaraTheme.colors
    return when (status) {
        AttendanceStatus.PRESENT -> Triple("Hadir", colors.success, colors.successSubtle)
        AttendanceStatus.LATE -> Triple("Terlambat", colors.warning, colors.warningSubtle)
        AttendanceStatus.PERMISSION -> Triple("Izin", colors.info, colors.infoSubtle)
        AttendanceStatus.SICK -> Triple("Sakit", colors.warning, colors.warningSubtle)
        AttendanceStatus.UNEXCUSED -> Triple("Alfa", colors.danger, colors.dangerSubtle)
        AttendanceStatus.UNKNOWN -> Triple("Belum Hadir", colors.textMuted, colors.surfaceSubtle)
    }
}
