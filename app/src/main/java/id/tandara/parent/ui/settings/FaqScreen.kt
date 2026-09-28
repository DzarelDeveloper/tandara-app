package id.tandara.parent.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.ui.components.TandaraTopAppBar

data class FaqItemData(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String
)

/**
 * Help Center & FAQ Screen (UI/UX V3 - Dark Navy Theme).
 */
@Composable
fun FaqScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var expandedFaqId by remember { mutableIntStateOf(-1) }

    val categories = listOf("Semua", "Presensi Wajah", "Pengajuan Izin", "Akun & Keamanan", "Notifikasi")

    val faqList = remember {
        listOf(
            FaqItemData(
                id = 1,
                category = "Presensi Wajah",
                question = "Bagaimana cara kerja presensi pengenalan wajah di sekolah?",
                answer = "Siswa cukup menghadap ke arah kamera mesin presensi atau tablet di gerbang sekolah. Sistem kecerdasan buatan Tandara mengenali fitur wajah secara instan tanpa kontak fisik, lalu mencatat waktu kehadiran ke database sekolah."
            ),
            FaqItemData(
                id = 2,
                category = "Presensi Wajah",
                question = "Berapa lama jeda waktu antara scan dan laporan masuk di aplikasi?",
                answer = "Laporan kehadiran terkirim secara instan melalui koneksi internet sekolah. Orang tua akan langsung melihat jam kedatangan dan status kehadiran pada aplikasi Tandara."
            ),
            FaqItemData(
                id = 3,
                category = "Presensi Wajah",
                question = "Mengapa status kehadiran anak saya tercatat 'Terlambat'?",
                answer = "Setiap sekolah memberlakukan batas jam kedatangan (misalnya pukul 07.00 WIB). Siswa yang melakukan pemindaian setelah jam tersebut otomatis tercatat sebagai Terlambat sesuai kebijakan disiplin sekolah."
            ),
            FaqItemData(
                id = 4,
                category = "Pengajuan Izin",
                question = "Bagaimana langkah mengajukan surat izin ketidakhadiran?",
                answer = "Pilih menu 'Izin', tentukan jenis ketidakhadiran (Sakit, Izin, atau Lainnya), tentukan tanggal, tuliskan alasan yang jelas, serta lampirkan surat bila ada. Tekan 'Kirim Pengajuan' untuk ditinjau oleh guru piket."
            ),
            FaqItemData(
                id = 5,
                category = "Pengajuan Izin",
                question = "Siapa yang memverifikasi surat izin yang diajukan orang tua?",
                answer = "Pengajuan izin diperiksa dan diverifikasi secara langsung oleh Guru Piket atau Wali Kelas melalui panel dashboard admin sekolah Tandara."
            ),
            FaqItemData(
                id = 6,
                category = "Akun & Keamanan",
                question = "Apakah foto wajah anak disimpan di aplikasi ponsel saya?",
                answer = "Tidak. Aplikasi Tandara di ponsel orang tua tidak menyimpan foto wajah maupun model biometrik apa pun. Seluruh privasi siswa dilindungi dengan aman di server sekolah oleh Tim Kena Scan."
            ),
            FaqItemData(
                id = 7,
                category = "Akun & Keamanan",
                question = "Mengapa akun saya hanya terhubung ke satu siswa?",
                answer = "Tandara Parent App menerapkan aturan ketat Satu Akun Orang Tua = Satu Siswa Terhubung untuk menjamin keamanan otentikasi data dan privasi siswa sekolah secara mutlak."
            ),
            FaqItemData(
                id = 8,
                category = "Notifikasi",
                question = "Mengapa notifikasi kehadiran belum muncul di perangkat saya?",
                answer = "Pastikan izin notifikasi untuk aplikasi Tandara telah aktif di pengaturan perangkat ponsel Anda agar pembaruan presensi dapat langsung diterima."
            )
        )
    }

    val filteredList = faqList.filter { item ->
        val matchesCategory = selectedCategory == "Semua" || item.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
                item.question.contains(searchQuery, ignoreCase = true) ||
                item.answer.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            TandaraTopAppBar(
                title = "Pusat Bantuan & FAQ",
                subtitle = "Jawaban pertanyaan seputar aplikasi",
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
                .testTag("faq_screen_content")
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Cari pertanyaan atau kata kunci...", fontSize = 13.5.sp, color = SecondaryText)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Cari",
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SecondarySurface,
                    unfocusedContainerColor = PrimarySurface,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = AccentBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("faq_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
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
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // FAQ Questions list
            if (filteredList.isNotEmpty()) {
                filteredList.forEach { item ->
                    val isExpanded = expandedFaqId == item.id
                    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimarySurface)
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                expandedFaqId = if (isExpanded) -1 else item.id
                            }
                            .padding(16.dp)
                            .testTag("faq_item_${item.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(SecondarySurface, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.HelpOutline,
                                            contentDescription = null,
                                            tint = AccentBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.question,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryText
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Tutup" else "Buka",
                                    tint = SecondaryText,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .rotate(rotationState)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = DividerColor)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = item.answer,
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp,
                                        color = SecondaryText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada hasil yang sesuai",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                        Text(
                            text = "Coba gunakan kata kunci pencarian yang lain.",
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact TU / Support Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SecondarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(PrimarySurface, CircleShape)
                            .border(1.dp, AccentBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Headphones,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Butuh Bantuan Lain?",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hubungi bagian Tata Usaha sekolah pada jam kerja.",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
