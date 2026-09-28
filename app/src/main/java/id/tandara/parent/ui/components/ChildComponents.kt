package id.tandara.parent.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.R
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.core.designsystem.SuccessGreenBg
import id.tandara.parent.domain.model.Student

/**
 * Permanent Student Identity Card for Tandara Parent App.
 * Strict One Parent Account = One Student.
 * No chevron, no selector, no dropdown, no multi-child UX.
 */
@Composable
fun PermanentStudentIdentityCard(
    student: Student?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PrimarySurface)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("permanent_student_identity_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Avatar
            if (student?.photoUrl?.contains("alya") == true || student?.id == "std_alya") {
                Image(
                    painter = painterResource(id = R.drawable.student_avatar_alya_1790336223122),
                    contentDescription = student.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, BorderColor, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SecondarySurface, CircleShape)
                        .border(1.5.dp, BorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student?.name ?: "Siswa",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${student?.className ?: "Kelas belum ditentukan"} • NIS ${student?.nisn ?: "-"}",
                    fontSize = 12.5.sp,
                    color = SecondaryText
                )
            }

            // Small status pill: ● Terhubung
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SuccessGreenBg)
                    .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(SuccessGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Terhubung",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
        }
    }
}

/**
 * Student Detail Profile Bottom Sheet (Non-technical, human-oriented).
 * Explains student affiliation, attendance status, and active face presence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailBottomSheet(
    student: Student?,
    todayStatus: String = "Belum melakukan presensi masuk",
    onDismiss: () -> Unit
) {
    if (student == null) return
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PrimarySurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.testTag("child_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profil Siswa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Tutup", tint = PrimaryText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student photo
            if (student.photoUrl?.contains("alya") == true || student.id == "std_alya") {
                Image(
                    painter = painterResource(id = R.drawable.student_avatar_alya_1790336223122),
                    contentDescription = student.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, BorderColor, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SecondarySurface, CircleShape)
                        .border(2.dp, BorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = student.name,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Kelas ${student.className} • NIS ${student.nisn}",
                fontSize = 13.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Info rows in elevated surface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SecondarySurface, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChildInfoRow(label = "Sekolah", value = "SMK Taman Harapan")
                HorizontalDivider(color = DividerColor)
                ChildInfoRow(
                    label = "Jurusan",
                    value = if (student.className.contains("RPL")) "Rekayasa Perangkat Lunak" else "Teknik Komputer Jaringan"
                )
                HorizontalDivider(color = DividerColor)
                ChildInfoRow(label = "Presensi Hari Ini", value = todayStatus)
                HorizontalDivider(color = DividerColor)
                // Human-readable enrollment state (no raw embeddings or biometric keys)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sistem Presensi", fontSize = 13.sp, color = SecondaryText)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Presensi wajah aktif",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ChildInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = SecondaryText)
        Text(text = value, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
    }
}
