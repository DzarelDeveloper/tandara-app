package id.tandara.parent.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.R
import id.tandara.parent.BuildConfig
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
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
import id.tandara.parent.ui.components.TandaraLogo
import id.tandara.parent.ui.components.TandaraTopAppBar

/**
 * About Tandara App & Team Screen (UI/UX V3 - Dark Navy Theme).
 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            TandaraTopAppBar(
                title = "Tentang Tandara",
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
                .testTag("about_screen_content"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo & Identity Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TandaraLogo(size = 64.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tandara Parent",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Pantau kehadiran, izin, dan informasi sekolah dalam satu aplikasi.",
                        fontSize = 13.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .background(SecondarySurface, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Versi ${BuildConfig.VERSION_NAME}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Developer Team: Kena Scan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.kenascan),
                        contentDescription = "Logo KenaScan",
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dikembangkan oleh",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KenaScan Team",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Innovating Intelligent School Solutions",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tandara dirancang dan dikembangkan oleh tim Kena Scan dengan visi menghubungkan ekosistem sekolah dan orang tua murid secara transparan, akurat, dan aman melalui teknologi Computer Vision pengenalan wajah pintar.",
                        fontSize = 13.sp,
                        color = SecondaryText,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Highlights
            Text(
                text = "Keunggulan Sistem Tandara",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            AboutHighlightCard(
                icon = Icons.Outlined.Bolt,
                iconTint = WarningAmber,
                iconBg = WarningAmberBg,
                title = "Pengenalan Wajah Sub-Detik",
                description = "Algoritma biometrik cepat mendeteksi kehadiran siswa tanpa antrean panjang di gerbang sekolah."
            )

            Spacer(modifier = Modifier.height(10.dp))

            AboutHighlightCard(
                icon = Icons.Outlined.Lock,
                iconTint = SuccessGreen,
                iconBg = SuccessGreenBg,
                title = "Privasi Terjaga & Terenkripsi",
                description = "Standar keamanan ketat tanpa penyimpanan foto di aplikasi orang tua dan perlindungan data terpusat."
            )

            Spacer(modifier = Modifier.height(10.dp))

            AboutHighlightCard(
                icon = Icons.Outlined.School,
                iconTint = AccentBlue,
                iconBg = PrimaryBlueLight,
                title = "Manajemen Izin & Rekap Terpadu",
                description = "Mempermudah orang tua mengajukan surat izin dan memantau persentase kehadiran bulanan siswa."
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tandara.id"))
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "Browser tidak tersedia.", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SecondarySurface,
                    contentColor = AccentBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_about_portfolio")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Website Tandara",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Copyright
            Text(
                text = "© 2026 Tim Kena Scan. Seluruh Hak Cipta Dilindungi.",
                fontSize = 11.5.sp,
                color = SecondaryText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun AboutHighlightCard(
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
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = SecondaryText,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
