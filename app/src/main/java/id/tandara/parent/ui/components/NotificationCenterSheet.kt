package id.tandara.parent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.ErrorRed
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.domain.model.ParentNotification

data class NotificationItemData(
    val id: String,
    val title: String,
    val studentName: String,
    val message: String,
    val time: String,
    val isUnread: Boolean,
    val type: NotificationType
)

enum class NotificationType {
    CHECK_IN,
    CHECK_OUT,
    LEAVE_APPROVED,
    LEAVE_REJECTED,
    UPDATED
}

/**
 * Dark Navy Notification Center Bottom Sheet.
 * Displays notifications strictly for the assigned student (Alya Putri).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    onDismiss: () -> Unit,
    notifications: List<ParentNotification> = emptyList(),
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedFilter by remember { mutableStateOf("Semua") }

    val notificationItems = remember(notifications) {
        notifications.mapIndexed { index, notification ->
            NotificationItemData(
                id = notification.id.ifBlank { "notif_$index" },
                title = notification.title.ifBlank { "Notifikasi" },
                studentName = notification.relatedStudentName ?: "Siswa",
                message = notification.message.ifBlank { "Informasi terbaru tersedia." },
                time = notification.timestamp.ifBlank { "Baru" },
                isUnread = !notification.isRead,
                type = when {
                    notification.type?.contains("LEAVE", ignoreCase = true) == true -> NotificationType.LEAVE_APPROVED
                    notification.title.contains("Pulang", ignoreCase = true) -> NotificationType.CHECK_OUT
                    notification.title.contains("Masuk", ignoreCase = true) -> NotificationType.CHECK_IN
                    else -> NotificationType.UPDATED
                }
            )
        }
    }

    val filteredList = notificationItems.filter {
        if (selectedFilter == "Belum Dibaca") it.isUnread else true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PrimarySurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = modifier.testTag("notification_center_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifikasi",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Tutup", tint = PrimaryText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs: Semua / Belum Dibaca
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Belum Dibaca").forEach { tab ->
                    val isSelected = selectedFilter == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = tab },
                        label = { Text(tab, fontSize = 12.5.sp) },
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
                        modifier = Modifier.testTag("notif_filter_${tab.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum ada notifikasi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Informasi presensi dan izin terbaru akan muncul di sini.",
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        val (icon, iconColor, iconBg) = getNotificationIconConfig(item.type)
                        val cardBg = if (item.isUnread) ElevatedSurface else SecondarySurface

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .border(
                                    1.dp,
                                    if (item.isUnread) AccentBlue.copy(alpha = 0.5f) else BorderColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp)
                                .testTag("notification_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Status Icon Container
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(iconBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )

                                        if (item.isUnread) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(AccentBlue, CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = item.message,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = SecondaryText
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.time,
                                        fontSize = 11.5.sp,
                                        color = SecondaryText.copy(alpha = 0.8f)
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

@Composable
private fun getNotificationIconConfig(type: NotificationType): Triple<ImageVector, Color, Color> {
    val colors = id.tandara.parent.core.designsystem.TandaraTheme.colors
    return when (type) {
        NotificationType.CHECK_IN -> Triple(Icons.Outlined.CheckCircle, colors.success, colors.successSubtle)
        NotificationType.CHECK_OUT -> Triple(Icons.Outlined.Home, colors.primary, colors.primarySubtle)
        NotificationType.LEAVE_APPROVED -> Triple(Icons.Outlined.Description, colors.success, colors.successSubtle)
        NotificationType.LEAVE_REJECTED -> Triple(Icons.Outlined.Close, colors.danger, colors.dangerSubtle)
        NotificationType.UPDATED -> Triple(Icons.Outlined.Schedule, colors.warning, colors.warningSubtle)
    }
}
