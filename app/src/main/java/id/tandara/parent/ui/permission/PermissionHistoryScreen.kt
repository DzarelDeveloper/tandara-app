package id.tandara.parent.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.common.DateUtils
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ErrorRed
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.core.designsystem.SuccessGreenBg
import id.tandara.parent.core.designsystem.WarningAmber
import id.tandara.parent.core.designsystem.WarningAmberBg
import id.tandara.parent.domain.model.LeaveRequest
import id.tandara.parent.domain.model.LeaveStatus
import id.tandara.parent.domain.model.Student
import id.tandara.parent.ui.components.ConnectionStateBanner
import id.tandara.parent.ui.components.TandaraTopAppBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Full permission history screen with pull-to-refresh support.
 * Shows all leave requests for the selected student with newest first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionHistoryScreen(
    viewModel: PermissionViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        topBar = {
            TandaraTopAppBar(
                title = "Riwayat Izin",
                onBackClick = onBackClick
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppBackground,
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refreshHistory() },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    ConnectionStateBanner(isOffline = uiState.isOffline, lastUpdatedAt = uiState.lastUpdatedAt)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (uiState.leaveHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimarySurface)
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when {
                                        uiState.selectedStudent == null -> "Data siswa belum tersedia."
                                        uiState.leaveHistoryAvailable -> "Belum ada riwayat izin."
                                        else -> "Riwayat izin belum dapat dimuat."
                                    },
                                    color = SecondaryText,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (uiState.selectedStudent != null && !uiState.leaveHistoryAvailable) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tarik ke bawah untuk mencoba lagi",
                                        color = AccentBlue,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    // Group by month if we have dates
                    val grouped = groupHistoryByMonth(uiState.leaveHistory)
                    grouped.forEach { (monthLabel, items) ->
                        item {
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryText,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(items, key = { it.id.ifBlank { "${it.startDate}_${it.submittedAt}" } }) { request ->
                            LeaveHistoryCard(
                                request = request,
                                student = uiState.selectedStudent,
                                modifier = Modifier.testTag("history_card_${request.id.ifBlank { request.startDate }}")
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * Group leave requests by month for display.
 */
private fun groupHistoryByMonth(requests: List<LeaveRequest>): Map<String, List<LeaveRequest>> {
    val localeId = Locale("id", "ID")
    val monthFmt = SimpleDateFormat("MMMM yyyy", localeId)
    val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val fallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    return requests.groupBy { req ->
        val dateStr = req.startDate.takeIf { it.isNotBlank() } ?: req.submittedAt
        val parsed = try {
            apiFmt.parse(dateStr)
        } catch (_: Exception) {
            try {
                fallback.parse(dateStr)
            } catch (_: Exception) {
                null
            }
        }
        parsed?.let { monthFmt.format(it) } ?: "Riwayat Lama"
    }.toSortedMap(compareByDescending { label ->
        // Sort by parsing month/year back to date
        try {
            monthFmt.parse(label)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    })
}
