package id.tandara.parent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.TandaraTheme
import id.tandara.parent.domain.model.AttendanceStatus
import id.tandara.parent.domain.model.LeaveStatus

enum class StatusVariant {
    SUCCESS,  // Hadir, Disetujui, Aktif
    WARNING,  // Terlambat, Sakit, Menunggu
    DANGER,   // Alfa, Ditolak
    INFO,     // Izin, Informasi
    NEUTRAL   // Belum presensi, Default
}

@Composable
fun StatusBadge(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TandaraStatusBadge(
    text: String,
    variant: StatusVariant,
    modifier: Modifier = Modifier,
    showDot: Boolean = false
) {
    val colors = TandaraTheme.colors
    val (textColor, bgColor) = when (variant) {
        StatusVariant.SUCCESS -> colors.success to colors.successSubtle
        StatusVariant.WARNING -> colors.warning to colors.warningSubtle
        StatusVariant.DANGER -> colors.danger to colors.dangerSubtle
        StatusVariant.INFO -> colors.info to colors.infoSubtle
        StatusVariant.NEUTRAL -> colors.textMuted to colors.surfaceSubtle
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .border(0.5.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(textColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AttendanceStatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val variant = when (status) {
        AttendanceStatus.PRESENT -> StatusVariant.SUCCESS
        AttendanceStatus.LATE -> StatusVariant.WARNING
        AttendanceStatus.PERMISSION -> StatusVariant.INFO
        AttendanceStatus.SICK -> StatusVariant.WARNING
        AttendanceStatus.UNEXCUSED -> StatusVariant.DANGER
        AttendanceStatus.UNKNOWN -> StatusVariant.NEUTRAL
    }
    TandaraStatusBadge(
        text = status.labelId,
        variant = variant,
        modifier = modifier,
        showDot = true
    )
}

@Composable
fun LeaveStatusBadge(
    status: LeaveStatus,
    modifier: Modifier = Modifier
) {
    val variant = when (status) {
        LeaveStatus.APPROVED -> StatusVariant.SUCCESS
        LeaveStatus.PENDING -> StatusVariant.WARNING
        LeaveStatus.REJECTED -> StatusVariant.DANGER
    }
    TandaraStatusBadge(
        text = status.displayName,
        variant = variant,
        modifier = modifier
    )
}
