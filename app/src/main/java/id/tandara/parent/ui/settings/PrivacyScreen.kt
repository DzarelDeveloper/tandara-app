package id.tandara.parent.ui.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.ErrorRed
import id.tandara.parent.core.designsystem.ErrorRedBg
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimaryBlueLight
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.core.designsystem.SuccessGreenBg
import id.tandara.parent.ui.components.TandaraTopAppBar

/**
 * Privacy & Security Policy Screen (UI/UX V3 - Dark Navy Theme).
 */
@Composable
fun PrivacyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    Scaffold(
        topBar = {
            TandaraTopAppBar(
                title = "Privasi & Keamanan",
                subtitle = "Kebijakan Data & Biometrik",
                onBackClick = onBackClick
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
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .testTag("privacy_screen_content")
        ) {
            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(SecondarySurface, CircleShape)
                            .border(1.dp, AccentBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privasi Data Terjamin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Komitmen perlindungan data siswa & keluarga oleh Tim Kena Scan.",
                            fontSize = 12.5.sp,
                            color = SecondaryText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Biometric Privacy Guarantees
            Text(
                text = "Prinsip Keamanan Biometrik Wajah",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(10.dp))

            PrivacyFeatureCard(
                icon = Icons.Outlined.NoPhotography,
                iconTint = ErrorRed,
                iconBg = ErrorRedBg,
                title = "Tanpa Penyimpanan Foto di Aplikasi",
                description = "Aplikasi orang tua ini TIDAK menyimpan foto wajah maupun embedding biometrik siswa pada penyimpanan lokal perangkat Anda. Hanya catatan status presensi yang diteruskan."
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrivacyFeatureCard(
                icon = Icons.Outlined.Storage,
                iconTint = AccentBlue,
                iconBg = PrimaryBlueLight,
                title = "Data Biometrik Terpusat di Server Sekolah",
                description = "Proses pencocokan pengenalan wajah dilakukan secara terisolasi oleh mesin dan server lokal sekolah yang terdaftar resmi, menjamin kedaulatan data siswa."
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrivacyFeatureCard(
                icon = Icons.Outlined.VerifiedUser,
                iconTint = SuccessGreen,
                iconBg = SuccessGreenBg,
                title = "Akses Khusus Wali Murid Sah",
                description = "Setiap akun orang tua diverifikasi melalui kontak resmi yang terdaftar di Tata Usaha sekolah. Anda hanya dapat melihat data anak perwalian Anda."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Technical & Security Standards
            Text(
                text = "Standar Enkripsi & Keamanan Sesi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SecurityBullet(
                        icon = Icons.Outlined.Lock,
                        title = "Enkripsi Jalur Transport (TLS 1.3)",
                        subtitle = "Seluruh pertukaran data antara aplikasi Tandara dan server terenkripsi penuh."
                    )
                    HorizontalDivider(color = DividerColor)
                    SecurityBullet(
                        icon = Icons.Outlined.Fingerprint,
                        title = "Penyimpanan Kredensial Aman",
                        subtitle = "Sesi login diamankan dengan sistem token sesi yang dapat dicabut sewaktu-waktu."
                    )
                    HorizontalDivider(color = DividerColor)
                    SecurityBullet(
                        icon = Icons.Outlined.Policy,
                        title = "Kepatuhan Regulasi PDP",
                        subtitle = "Mengadopsi prinsip perlindungan data pribadi dan transparansi audit sistem kehadiran."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Contact & Rights
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimarySurface)
                    .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hak & Permintaan Data",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Orang tua berhak mengajukan koreksi atau klarifikasi riwayat kehadiran anak kapan saja kepada Wali Kelas atau pihak Tata Usaha sekolah.",
                            fontSize = 12.5.sp,
                            color = SecondaryText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun PrivacyFeatureCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PrimarySurface)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.5.sp,
                    color = SecondaryText,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SecurityBullet(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SecondaryText,
                lineHeight = 16.sp
            )
        }
    }
}
