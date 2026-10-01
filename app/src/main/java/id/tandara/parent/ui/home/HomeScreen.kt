package id.tandara.parent.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import id.tandara.parent.core.designsystem.BorderColorLight
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.MutedText
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.core.designsystem.SuccessGreenBg
import id.tandara.parent.core.designsystem.WarningAmber
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.ui.components.ChildDetailBottomSheet
import id.tandara.parent.ui.components.ConnectionStateBanner
import id.tandara.parent.ui.components.NotificationCenterSheet
import id.tandara.parent.ui.components.PermanentStudentIdentityCard
import id.tandara.parent.ui.components.RealtimeAttendanceBanner
import id.tandara.parent.ui.components.TandaraBrandTopAppBar

/**
 * Home Screen (UI/UX V3).
 * Redesigned with Dark Navy Layered Interface & Strict One Parent Account = One Student.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToReports: () -> Unit,
    onNavigateToPermission: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val todayAttendance = uiState.todayAttendance
    val hasRecordedAttendance = todayAttendance?.hasRecordedAttendance == true

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    // 1. Notification Center Bottom Sheet
    if (uiState.showNotificationSheet) {
        NotificationCenterSheet(
            onDismiss = { viewModel.closeNotificationSheet() },
            notifications = uiState.notifications
        )
    }

    // 2. Student Detail Profile Bottom Sheet
    if (uiState.showStudentDetail) {
        val student = uiState.currentStudent
        val statusText = when (todayAttendance?.status) {
            AttendanceStatus.PRESENT -> "Sudah tiba di sekolah (${todayAttendance.checkInTime ?: "Belum tercatat"} WIB)"
            AttendanceStatus.LATE -> "Tiba di sekolah • Terlambat (${todayAttendance.checkInTime ?: "Belum tercatat"} WIB)"
            AttendanceStatus.PERMISSION -> "Izin Disetujui"
            AttendanceStatus.SICK -> "Sakit"
            else -> "Belum melakukan presensi masuk"
        }
        ChildDetailBottomSheet(
            student = student,
            todayStatus = statusText,
            onDismiss = { viewModel.closeStudentDetail() }
        )
    }

    Scaffold(
        topBar = {
            TandaraBrandTopAppBar(
                onNotificationClick = { viewModel.openNotificationSheet() },
                tagline = "SMK Taman Harapan",
                hasUnreadNotifications = uiState.notifications.any { !it.isRead }
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
                .testTag("home_screen_content")
        ) {
            // Offline Banner if applicable
            ConnectionStateBanner(isOffline = uiState.isOffline, lastUpdatedAt = uiState.lastUpdatedAt)

            // Realtime Attendance In-App Banner
            RealtimeAttendanceBanner(
                visible = uiState.showRealtimeBanner,
                title = "Presensi masuk berhasil",
                message = uiState.todayAttendance?.checkInTime?.let {
                    "${uiState.currentStudent?.name ?: "Siswa"} tiba pukul ${it} WIB"
                } ?: "Belum ada presensi hari ini.",
                onDismiss = { viewModel.dismissRealtimeBanner() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Greeting Section (Personal, warm, human)
            Column {
                Text(
                    text = "${uiState.greeting}, ${uiState.currentParent?.name ?: "Orang Tua/Wali"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (!hasRecordedAttendance) "Belum ada data kehadiran hari ini." else "Berikut status kehadiran anak Anda hari ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permanent Student Identity Card (Strict One Account = One Student, NO dropdown)
            PermanentStudentIdentityCard(
                student = uiState.currentStudent,
                onClick = { viewModel.openStudentDetail() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // PRIMARY ATTENDANCE CARD (Visual Focus of Home)
            // ========================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElevatedSurface)
                    .border(1.dp, BorderColorLight, RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .testTag("primary_attendance_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRESENSI HARI INI",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            letterSpacing = 1.sp
                        )

                        // Date badge
                        Text(
                            text = uiState.formattedDate.ifEmpty { "Hari ini" },
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val todayStatusText = when {
                        !uiState.todayAttendanceAvailable -> "Data presensi belum tersedia"
                        !hasRecordedAttendance -> "Belum ada presensi"
                        todayAttendance.status == AttendanceStatus.PRESENT -> "Sudah tiba di sekolah"
                        todayAttendance.status == AttendanceStatus.LATE -> "Terlambat masuk"
                        todayAttendance.status == AttendanceStatus.PERMISSION -> "Izin diterima"
                        todayAttendance.status == AttendanceStatus.SICK -> "Sakit"
                        else -> "Belum tercatat"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    when (todayAttendance?.status) {
                                        AttendanceStatus.PRESENT -> SuccessGreen
                                        AttendanceStatus.LATE -> WarningAmber
                                        AttendanceStatus.PERMISSION, AttendanceStatus.SICK -> AccentBlue
                                        else -> SecondaryText
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = todayStatusText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (hasRecordedAttendance) (todayAttendance?.checkInTime ?: "-") else "-",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (hasRecordedAttendance) PrimaryText else SecondaryText,
                            lineHeight = 36.sp
                        )
                        if (hasRecordedAttendance) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WIB",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryText,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        if (hasRecordedAttendance) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (todayAttendance?.punctualityLabel != null) SuccessGreenBg else SecondarySurface)
                                    .border(1.dp, if (todayAttendance?.punctualityLabel != null) SuccessGreen.copy(alpha = 0.3f) else DividerColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                if (todayAttendance?.punctualityLabel != null) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = todayAttendance?.punctualityLabel ?: "Belum tercatat",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (todayAttendance?.punctualityLabel != null) SuccessGreen else SecondaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Check-in & Check-out Visual (Strong numbers, clear hierarchy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Masuk
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Masuk",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (todayAttendance?.checkInTime != null) "${todayAttendance.checkInTime} WIB" else if (uiState.todayAttendanceAvailable) "Belum tercatat" else "Belum tersedia",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayAttendance?.checkInTime != null) PrimaryText else SecondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = todayAttendance?.punctualityLabel ?: if (uiState.todayAttendanceAvailable) "Belum tercatat" else "Belum tersedia",
                                fontSize = 11.5.sp,
                                color = if (todayAttendance?.punctualityLabel != null) SuccessGreen else SecondaryText
                            )
                        }

                        VerticalDivider(
                            color = DividerColor,
                            modifier = Modifier
                                .height(48.dp)
                                .padding(horizontal = 16.dp)
                        )

                        // Pulang
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pulang",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (todayAttendance?.checkOutTime != null) "${todayAttendance.checkOutTime} WIB" else if (uiState.todayAttendanceAvailable) "Belum tercatat" else "Belum tersedia",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayAttendance?.checkOutTime != null) PrimaryText else SecondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (todayAttendance?.checkOutTime != null) "Selesai" else if (uiState.todayAttendanceAvailable) "Belum tercatat" else "Belum tersedia",
                                fontSize = 11.5.sp,
                                color = if (todayAttendance?.checkOutTime != null) SuccessGreen else SecondaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Vertical Timeline (Readable at a glance without opening another page)
                    Text(
                        text = "Alur Presensi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (todayAttendance == null) {
                        Text(
                            text = if (uiState.todayAttendanceAvailable) {
                                "Belum ada log presensi untuk hari ini."
                            } else {
                                "Data presensi hari ini belum dapat dimuat."
                            },
                            fontSize = 12.5.sp,
                            color = SecondaryText
                        )
                    } else {
                        // Timeline Step 1: Check In
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(36.dp)
                                        .background(DividerColor)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (todayAttendance.checkInTime != null) "${todayAttendance.checkInTime} WIB" else "Belum tercatat",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (todayAttendance.checkInTime != null) PrimaryText else SecondaryText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Presensi Masuk",
                                        fontSize = 13.sp,
                                        color = SecondaryText
                                    )
                                }
                                Text(
                                    text = if (todayAttendance.checkInTime != null) "Berhasil diverifikasi oleh sistem sekolah" else "Belum ada data masuk hari ini",
                                    fontSize = 12.sp,
                                    color = if (todayAttendance.checkInTime != null) SuccessGreen else SecondaryText
                                )
                            }
                        }

                        // Timeline Step 2: Check Out
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .border(2.dp, SecondaryText, CircleShape)
                                        .background(ElevatedSurface, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (todayAttendance.checkOutTime != null) "${todayAttendance.checkOutTime} WIB" else "Belum tercatat",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (todayAttendance.checkOutTime != null) PrimaryText else SecondaryText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Presensi Pulang",
                                        fontSize = 13.sp,
                                        color = SecondaryText
                                    )
                                }
                                Text(
                                    text = if (todayAttendance.checkOutTime != null) "Presensi pulang tercatat" else "Belum tercatat (menunggu jam kepulangan)",
                                    fontSize = 12.sp,
                                    color = if (todayAttendance.checkOutTime != null) SuccessGreen else SecondaryText.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // QUICK ACTIONS (Visually Differentiated: Primary vs Secondary)
            // ========================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action: Ajukan Izin
                Button(
                    onClick = onNavigateToPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_ajukan_izin")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ajukan Izin",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                }

                // Secondary Action: Lihat Laporan
                OutlinedButton(
                    onClick = onNavigateToReports,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SecondarySurface,
                        contentColor = PrimaryText
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_lihat_laporan")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lihat Laporan",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========================================================
            // MONTHLY SUMMARY (One Horizontal Surface with Internal Dividers)
            // ========================================================
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ringkasan ${uiState.monthYearText.ifEmpty { "Bulan Ini" }}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToReports)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "Rincian",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentBlue
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Rincian",
                            tint = AccentBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimarySurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryColumnItem(count = uiState.monthlySummary?.presentCount?.toString() ?: "—", label = "Hadir", countColor = SuccessGreen)
                        VerticalDivider(color = DividerColor, modifier = Modifier.height(36.dp))
                        SummaryColumnItem(count = uiState.monthlySummary?.lateCount?.toString() ?: "—", label = "Terlambat", countColor = WarningAmber)
                        VerticalDivider(color = DividerColor, modifier = Modifier.height(36.dp))
                        SummaryColumnItem(count = uiState.monthlySummary?.permissionCount?.toString() ?: "—", label = "Izin", countColor = AccentBlue)
                        VerticalDivider(color = DividerColor, modifier = Modifier.height(36.dp))
                        SummaryColumnItem(count = uiState.monthlySummary?.unexcusedCount?.toString() ?: "—", label = "Alfa", countColor = SecondaryText)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryColumnItem(
    count: String,
    label: String,
    countColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp)
    ) {
        Text(
            text = count,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = countColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = SecondaryText
        )
    }
}
