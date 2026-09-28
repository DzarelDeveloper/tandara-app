package id.tandara.parent.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.TandaraTheme
import id.tandara.parent.ui.components.TandaraButton
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val visualType: OnboardingVisualType
)

enum class OnboardingVisualType {
    ATTENDANCE_MONITORING,
    REALTIME_INFORMATION,
    EASY_LEAVE_REQUEST
}

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    onBackToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TandaraTheme.colors
    val pages = listOf(
        OnboardingPageData(
            title = "Pantau Kehadiran",
            description = "Lihat waktu kedatangan dan kepulangan anak langsung dari Tandara.",
            visualType = OnboardingVisualType.ATTENDANCE_MONITORING
        ),
        OnboardingPageData(
            title = "Informasi Secara Realtime",
            description = "Ketahui saat informasi presensi anak tercatat di sekolah.",
            visualType = OnboardingVisualType.REALTIME_INFORMATION
        ),
        OnboardingPageData(
            title = "Izin Lebih Praktis",
            description = "Ajukan izin dan pantau status pengajuan dalam satu aplikasi.",
            visualType = OnboardingVisualType.EASY_LEAVE_REQUEST
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (pagerState.currentPage > 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        } else {
            onBackToWelcome()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("screen_onboarding")
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Safe Area & "Lewati" (Skip) Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onFinishOnboarding,
                        modifier = Modifier.testTag("button_onboarding_skip")
                    ) {
                        Text(
                            text = "Lewati",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // 2. Pager Content (Illustration 38-42% + Title + Description)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Illustration container (approx 38-42% of viewport height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 380.dp)
                                .fillMaxWidth()
                        ) {
                            when (page.visualType) {
                                OnboardingVisualType.ATTENDANCE_MONITORING -> {
                                    OnboardingAttendanceVisual()
                                }
                                OnboardingVisualType.REALTIME_INFORMATION -> {
                                    OnboardingRealtimeVisual()
                                }
                                OnboardingVisualType.EASY_LEAVE_REQUEST -> {
                                    OnboardingLeaveVisual()
                                }
                            }
                        }
                    }

                    // Text Content Block
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.textSecondary,
                                lineHeight = 22.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .widthIn(max = 340.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // 3. Bottom Controls: Animated Page Indicators + Primary Action Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .testTag("onboarding_page_indicator")
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            label = "indicator_width"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) colors.primary else colors.border,
                            label = "indicator_color"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                // Primary Button: Lanjut vs Masuk ke Tandara
                Box(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                ) {
                    if (pagerState.currentPage < pages.size - 1) {
                        TandaraButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            testTag = "button_onboarding_next"
                        ) {
                            Text(
                                text = "Lanjut",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        TandaraButton(
                            onClick = onFinishOnboarding,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            testTag = "button_onboarding_finish"
                        ) {
                            Text(
                                text = "Masuk ke Tandara",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual Concept 1: Student arriving at SMK Taman Harapan with attendance success indicator.
 */
@Composable
private fun OnboardingAttendanceVisual() {
    val colors = TandaraTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp)),
        color = colors.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header with school badge & live status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SMK Taman Harapan",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )
                        Text(
                            text = "Gerbang Presensi Siswa",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textMuted
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.successSubtle,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, colors.success.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Tepat Waktu",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.success,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student arrival visual card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.5.dp, colors.success, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Siswa Tiba",
                            tint = colors.success,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Alya Putri",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Presensi Masuk: 06:47 WIB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.success
                            )
                        )
                        Text(
                            text = "Jadwal Kepulangan: 15:30 WIB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textMuted
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Concept 2: Parent holding phone + attendance update from school.
 */
@Composable
private fun OnboardingRealtimeVisual() {
    val colors = TandaraTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp)),
        color = colors.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header: Realtime event tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.infoSubtle,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, colors.info.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = colors.info,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pembaruan Sekolah",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.info,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Text(
                    text = "Baru Saja",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phone notification card representation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.primarySubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Presensi Masuk Tercatat",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Alya Putri telah melakukan presensi di gerbang SMK Taman Harapan.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textSecondary,
                                lineHeight = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Waktu: 06:47:18 WIB • Verifikasi Valid",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Concept 3: Parent submitting leave -> school -> approval status.
 */
@Composable
private fun OnboardingLeaveVisual() {
    val colors = TandaraTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp)),
        color = colors.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pengajuan Izin Mandiri",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )
                        Text(
                            text = "Langsung dari aplikasi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textMuted
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.successSubtle,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, colors.success.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Disetujui",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.success,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Leave flow illustration card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Izin Sakit • 1 Hari",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Surat dokter terverifikasi oleh guru piket SMK Taman Harapan.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verifikator: Guru Piket",
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
                        )
                        Text(
                            text = "Status: Sah",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.success,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
